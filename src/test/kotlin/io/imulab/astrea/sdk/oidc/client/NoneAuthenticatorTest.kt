package io.imulab.astrea.sdk.oidc.client

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import io.imulab.astrea.sdk.oauth.client.ClientLookup
import io.imulab.astrea.sdk.oauth.error.OAuthException
import io.imulab.astrea.sdk.oauth.request.OAuthRequestForm
import io.imulab.astrea.sdk.oauth.reserved.ClientType
import io.imulab.astrea.sdk.oauth.reserved.GrantType
import io.imulab.astrea.sdk.oauth.reserved.Param
import io.imulab.astrea.sdk.oidc.client.authn.NoneAuthenticator
import io.imulab.astrea.sdk.oidc.request.OidcRequestForm
import io.imulab.astrea.sdk.oidc.reserved.AuthenticationMethod
import io.kotlintest.should
import io.kotlintest.shouldBe
import io.kotlintest.specs.BehaviorSpec
import kotlinx.coroutines.runBlocking

class NoneAuthenticatorTest : BehaviorSpec({

    Given("None authenticator") {
        val authenticator = exampleAuthenticator()

        When("Tested supported methods") {
            Then("Should support none") {
                authenticator.supports(AuthenticationMethod.none) shouldBe true

            }

            Then("Should not support private_key_jwt") {
                authenticator.supports(AuthenticationMethod.privateKeyJwt) shouldBe false
            }
        }

        When("A public client authenticates") {
            val result = runCatching {
                runBlocking { authenticator.authenticate(exampleForm("foo")) }
            }

            then("should pass authentication") {
                result.isSuccess shouldBe true
                result.getOrNull()?.id shouldBe "foo"
            }
        }

        When("A implicit flow only client authenticates") {
            val result = runCatching {
                runBlocking { authenticator.authenticate(exampleForm("bar")) }
            }

            then("should pass authentication") {
                result.isSuccess shouldBe true
                result.getOrNull()?.id shouldBe "bar"
            }
        }

        When("A confidential client with multiple grant types authenticates") {
            val result = runCatching {
                runBlocking { authenticator.authenticate(exampleForm("zoo")) }
            }

            then("should be rejected") {
                result.isFailure shouldBe true
                result.exceptionOrNull() should { t -> t is OAuthException }
            }
        }
    }

}) {

    companion object {
        val exampleAuthenticator : () -> NoneAuthenticator = {
            val publicClient = mock<OidcClient> {
                onGeneric { id } doReturn "foo"
                onGeneric { type } doReturn ClientType.public
            }

            val implicitOnlyClient = mock<OidcClient> {
                onGeneric { id } doReturn "bar"
                onGeneric { type } doReturn ClientType.confidential
                onGeneric { grantTypes } doReturn setOf(GrantType.implicit)
            }

            val zooClient = mock<OidcClient> {
                onGeneric { id } doReturn "zoo"
                onGeneric { type } doReturn ClientType.confidential
                onGeneric { grantTypes } doReturn setOf(GrantType.implicit, GrantType.authorizationCode)
            }

            val clientLookup = mock<ClientLookup> {
                onBlocking { find("foo") } doReturn publicClient
                onBlocking { find("bar") } doReturn implicitOnlyClient
                onBlocking { find("zoo") } doReturn zooClient
            }

            NoneAuthenticator(clientLookup = clientLookup)
        }

        val exampleForm : (String) -> OAuthRequestForm = { clientId ->
            OidcRequestForm(
                    mutableMapOf(
                            Param.clientId to listOf(clientId)
                    )
            )
        }
    }
}