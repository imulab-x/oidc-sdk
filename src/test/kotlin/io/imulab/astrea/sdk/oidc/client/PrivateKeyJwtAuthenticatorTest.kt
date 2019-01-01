package io.imulab.astrea.sdk.oidc.client

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import io.imulab.astrea.sdk.oauth.OAuthContext
import io.imulab.astrea.sdk.oauth.client.ClientLookup
import io.imulab.astrea.sdk.oauth.error.OAuthException
import io.imulab.astrea.sdk.oauth.request.OAuthRequestForm
import io.imulab.astrea.sdk.oauth.reserved.Param
import io.imulab.astrea.sdk.oauth.token.JwtSigningAlgorithm
import io.imulab.astrea.sdk.oauth.token.resolvePrivateKey
import io.imulab.astrea.sdk.oidc.client.authn.PrivateKeyJwtAuthenticator
import io.imulab.astrea.sdk.oidc.jwk.JsonWebKeySetStrategy
import io.imulab.astrea.sdk.oidc.request.OidcRequestForm
import io.imulab.astrea.sdk.oidc.reserved.AuthenticationMethod
import io.imulab.astrea.sdk.oidc.reserved.OidcParam
import io.imulab.astrea.sdk.oidc.reserved.jwtBearerClientAssertionType
import io.kotlintest.should
import io.kotlintest.shouldBe
import io.kotlintest.specs.BehaviorSpec
import kotlinx.coroutines.runBlocking
import org.jose4j.jwk.JsonWebKey
import org.jose4j.jwk.JsonWebKeySet
import org.jose4j.jwk.RsaJwkGenerator
import org.jose4j.jwk.Use
import org.jose4j.jws.JsonWebSignature
import org.jose4j.jwt.JwtClaims
import org.jose4j.keys.AesKey

class PrivateKeyJwtAuthenticatorTest : BehaviorSpec({

    Given("A configured authenticator") {
        val clientSecret = "7b56604a383f405583736ca5539c48d9".toByteArray()
        val authenticator = exampleAuthenticator(clientSecret)

        When("Tested supported methods") {
            Then("Should support private_key_jwt") {
                authenticator.supports(AuthenticationMethod.privateKeyJwt) shouldBe true
            }
            Then("Should not support client_secret_jwt") {
                authenticator.supports(AuthenticationMethod.clientSecretJwt) shouldBe false
            }
        }

        When("authenticated with token signed by correct exampleKey") {
            val result = runCatching {
                runBlocking { authenticator.authenticate(exampleFormOne()) }
            }

            Then("should have passed authentication") {
                result.isSuccess shouldBe true
            }

            Then("should have return the authenticated client") {
                result.getOrNull()?.id shouldBe "foo"
            }
        }

        When("authenticated with token signed by correct exampleKey, but without kid header hint") {
            val result = runCatching {
                runBlocking { authenticator.authenticate(exampleFormTwo()) }
            }

            Then("should have passed authentication") {
                result.isSuccess shouldBe true
            }

            Then("should have return the authenticated client") {
                result.getOrNull()?.id shouldBe "foo"
            }
        }

        When("authenticated with token signed by correct exampleKey, but with incorrect assertion type") {
            val result = runCatching {
                runBlocking { authenticator.authenticate(exampleFormOne().also {
                    it.httpForm[OidcParam.clientAssertionType] = listOf("foo")
                }) }
            }

            Then("should have been rejected") {
                result.isFailure shouldBe true
                result.exceptionOrNull() should { t -> t is OAuthException }
            }
        }

        When("authenticated with token signed by incorrect exampleKey") {
            val incorrectKey = RsaJwkGenerator.generateJwk(2048).also { k ->
                k.use = Use.SIGNATURE
                k.keyId = "27ca1312-5932-4146-b41d-a055929d728d"
                k.algorithm = JwtSigningAlgorithm.RS256.algorithmIdentifier
            }
            val result = runCatching {
                runBlocking { authenticator.authenticate(exampleFormOne(incorrectKey)) }
            }

            Then("should have been rejected") {
                result.isFailure shouldBe true
                result.exceptionOrNull() should { it is OAuthException }
            }
        }

        When("authenticated with token signed with client secret") {
            val result = runCatching {
                runBlocking { authenticator.authenticate(exampleFormThree(clientSecret)) }
            }

            Then("should have passed authentication") {
                result.isSuccess shouldBe true
            }

            Then("should have return the authenticated client") {
                result.getOrNull()?.id shouldBe "foo"
            }
        }

        When("authenticated with token signed with incorrect client secret") {
            val incorrectSecret = "AED09DD1844B4D19ABAC73A82F4A96F1".toByteArray()
            val result = runCatching {
                runBlocking { authenticator.authenticate(exampleFormThree(incorrectSecret)) }
            }

            Then("should have been rejected") {
                result.isFailure shouldBe true
                result.exceptionOrNull() should { it is OAuthException }
            }
        }
    }

}) {
    companion object {
        private val exampleKey: JsonWebKey = RsaJwkGenerator.generateJwk(2048).also { k ->
            k.use = Use.SIGNATURE
            k.keyId = "4f043cea-f8f3-4367-a1e0-76dd402fe4f4"
            k.algorithm = JwtSigningAlgorithm.RS256.algorithmIdentifier
        }

        val exampleAuthenticator : (ByteArray) -> PrivateKeyJwtAuthenticator = { clientSecret ->
            val clientJwksStrategy = mock<JsonWebKeySetStrategy> {
                onBlocking { resolveKeySet(any()) } doReturn JsonWebKeySet().also { s -> s.addJsonWebKey(exampleKey) }
            }

            val fooClient = mock<OidcClient> {
                onGeneric { id } doReturn "foo"
                onGeneric { secret } doReturn clientSecret
            }

            val clientLookup = mock<ClientLookup> {
                onBlocking { find("foo") } doReturn fooClient
            }

            val serverContext = mock<OAuthContext> {
                onGeneric { tokenEndpointUrl } doReturn "https://nix.com/oauth/token"
            }

            PrivateKeyJwtAuthenticator(
                    clientLookup = clientLookup,
                    clientJwksStrategy = clientJwksStrategy,
                    oauthContext = serverContext
            )
        }

        /**
         * Provides a form with authentication set
         */
        fun exampleFormOne(key: JsonWebKey = exampleKey): OAuthRequestForm {
            val token = JsonWebSignature().also { jws ->
                jws.key = exampleKey.resolvePrivateKey()
                jws.algorithmHeaderValue = JwtSigningAlgorithm.RS256.spec
                jws.keyIdHeaderValue = key.keyId
                jws.payload = JwtClaims().also { c ->
                    c.setGeneratedJwtId()
                    c.setIssuedAtToNow()
                    c.setExpirationTimeMinutesInTheFuture(10f)
                    c.subject = "foo"
                    c.issuer = "foo"
                    c.setAudience("https://nix.com/oauth/token")
                }.toJson()
            }.compactSerialization

            return OidcRequestForm(
                    mutableMapOf(
                            Param.clientId to listOf("foo"),
                            OidcParam.clientAssertion to listOf(token),
                            OidcParam.clientAssertionType to listOf(jwtBearerClientAssertionType)
                    )
            )
        }

        /**
         * Provides a form with authentication set, but kid header of the token is not set.
         */
        val exampleFormTwo: () -> OAuthRequestForm = {
            val token = JsonWebSignature().also { jws ->
                jws.key = exampleKey.resolvePrivateKey()
                jws.algorithmHeaderValue = JwtSigningAlgorithm.RS256.spec
                jws.payload = JwtClaims().also { c ->
                    c.setGeneratedJwtId()
                    c.setIssuedAtToNow()
                    c.setExpirationTimeMinutesInTheFuture(10f)
                    c.subject = "foo"
                    c.issuer = "foo"
                    c.setAudience("https://nix.com/oauth/token")
                }.toJson()
            }.compactSerialization

            OidcRequestForm(
                    mutableMapOf(
                            Param.clientId to listOf("foo"),
                            OidcParam.clientAssertion to listOf(token),
                            OidcParam.clientAssertionType to listOf(jwtBearerClientAssertionType)
                    )
            )
        }

        /**
         * Provides a form with authentication set, with token signed by a secret using HS256.
         */
        val exampleFormThree: (ByteArray) -> OAuthRequestForm = { clientSecret ->
            val token: String = JsonWebSignature().also { jws ->
                jws.key = AesKey(clientSecret)
                jws.algorithmHeaderValue = JwtSigningAlgorithm.HS256.spec
                jws.payload = JwtClaims().also { c ->
                    c.setGeneratedJwtId()
                    c.setIssuedAtToNow()
                    c.setExpirationTimeMinutesInTheFuture(10f)
                    c.subject = "foo"
                    c.issuer = "foo"
                    c.setAudience("https://nix.com/oauth/token")
                }.toJson()
            }.compactSerialization

            OidcRequestForm(
                    mutableMapOf(
                            Param.clientId to listOf("foo"),
                            OidcParam.clientAssertion to listOf(token),
                            OidcParam.clientAssertionType to listOf(jwtBearerClientAssertionType)
                    )
            )
        }
    }
}