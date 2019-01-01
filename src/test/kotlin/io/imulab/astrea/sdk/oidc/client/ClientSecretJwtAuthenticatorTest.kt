package io.imulab.astrea.sdk.oidc.client

import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import io.imulab.astrea.sdk.oauth.OAuthContext
import io.imulab.astrea.sdk.oauth.client.ClientLookup
import io.imulab.astrea.sdk.oauth.error.InvalidClient
import io.imulab.astrea.sdk.oauth.error.OAuthException
import io.imulab.astrea.sdk.oauth.request.OAuthRequestForm
import io.imulab.astrea.sdk.oauth.reserved.Param
import io.imulab.astrea.sdk.oauth.token.JwtSigningAlgorithm
import io.imulab.astrea.sdk.oidc.client.authn.ClientSecretJwtAuthenticator
import io.imulab.astrea.sdk.oidc.request.OidcRequestForm
import io.imulab.astrea.sdk.oidc.reserved.AuthenticationMethod
import io.imulab.astrea.sdk.oidc.reserved.OidcParam
import io.imulab.astrea.sdk.oidc.reserved.jwtBearerClientAssertionType
import io.kotlintest.should
import io.kotlintest.shouldBe
import io.kotlintest.specs.BehaviorSpec
import kotlinx.coroutines.runBlocking
import org.jose4j.jws.JsonWebSignature
import org.jose4j.jwt.JwtClaims
import org.jose4j.keys.AesKey

class ClientSecretJwtAuthenticatorTest : BehaviorSpec({

    Given("Authenticator and correct client secret") {
        val correctSecret = "af7132dd14df40888abc4af88a68cde9".toByteArray()
        val authenticator = exampleAuthenticator(correctSecret)

        When("Tested supported methods") {
            Then("Should support client_secret_jwt") {
                authenticator.supports(AuthenticationMethod.clientSecretJwt) shouldBe true
            }

            Then("Should not support private_key_jwt") {
                authenticator.supports(AuthenticationMethod.privateKeyJwt) shouldBe false
            }
        }

        When("using token signed with correct client secret") {
            val result = runCatching {
                runBlocking { authenticator.authenticate(exampleForm(correctSecret)) }
            }

            Then("should have passed authentication") {
                result.isSuccess shouldBe true
            }

            then("should have return the authenticated client") {
                result.getOrNull()?.id shouldBe "foo"
            }
        }

        `when`("using token signed with correct client secret, but with incorrect assertion type") {
            val result = runCatching {
                runBlocking {
                    authenticator.authenticate(exampleForm(correctSecret).also {
                        it.httpForm[OidcParam.clientAssertionType] = listOf("foo")
                    })
                }
            }

            then("should have been rejected") {
                result.isFailure shouldBe true
                result.exceptionOrNull() should { t -> t is OAuthException }
            }
        }

        `when`("using token signed with incorrect client secret") {
            val incorrectSecret = "7b1e1a62685a4d9eb12ec38b14f49d54".toByteArray()
            val result = runCatching {
                runBlocking { authenticator.authenticate(exampleForm(incorrectSecret)) }
            }

            then("should have passed authentication") {
                result.isFailure shouldBe true
                result.exceptionOrNull() should { t -> t is OAuthException }
            }
        }
    }


}) {
    companion object {
        val exampleAuthenticator: (ByteArray) -> ClientSecretJwtAuthenticator = { clientSecret ->
            val client = mock<OidcClient> {
                onGeneric { id } doReturn "foo"
                onGeneric { secret } doReturn clientSecret
            }

            val clientLookup = mock<ClientLookup> {
                onBlocking { find("foo") } doReturn client
                onBlocking { find(argThat { this != "foo" }) } doAnswer {
                    throw InvalidClient.unknown()
                }
            }

            val serverContext = mock<OAuthContext> {
                onGeneric { tokenEndpointUrl } doReturn "https://nix.com/oauth/token"
            }

            ClientSecretJwtAuthenticator(clientLookup, serverContext)
        }

        val exampleForm: (ByteArray) -> OAuthRequestForm = { clientSecret ->
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