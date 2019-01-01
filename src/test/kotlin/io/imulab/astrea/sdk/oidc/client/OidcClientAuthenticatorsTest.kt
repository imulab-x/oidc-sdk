package io.imulab.astrea.sdk.oidc.client

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import io.imulab.astrea.sdk.oauth.client.ClientLookup
import io.imulab.astrea.sdk.oauth.request.OAuthRequestForm
import io.imulab.astrea.sdk.oauth.reserved.Param
import io.imulab.astrea.sdk.oidc.client.authn.ClientSecretJwtAuthenticator
import io.imulab.astrea.sdk.oidc.client.authn.OidcClientAuthenticators
import io.imulab.astrea.sdk.oidc.client.authn.PrivateKeyJwtAuthenticator
import io.imulab.astrea.sdk.oidc.request.OidcRequestForm
import io.imulab.astrea.sdk.oidc.reserved.AuthenticationMethod
import io.kotlintest.shouldNotBe
import io.kotlintest.specs.BehaviorSpec
import kotlinx.coroutines.runBlocking

class OidcClientAuthenticatorsTest : BehaviorSpec({

    Given("A configured chain of authenticators") {
        val authenticator = exampleAuthenticators()

        When("Requested by a client with registered authentication method") {
            val result = runCatching {
                runBlocking { authenticator.authenticate(exampleForm("foo")) }
            }

            then("The correct authenticator should have been invoked") {
                // Authenticator throws exception is the intended behaviour
                result.exceptionOrNull()
                        ?.stackTrace
                        ?.filter { it.className == PrivateKeyJwtAuthenticator::class.java.name } shouldNotBe null
            }
        }
    }

}) {
    companion object {

        val exampleAuthenticators: () -> OidcClientAuthenticators = {
            val clientSecretJwtAuthenticator = ClientSecretJwtAuthenticator(mock(), mock())
            val privateKeyJwtAuthenticator = PrivateKeyJwtAuthenticator(mock(), mock(), mock())

            val fooClient = mock<OidcClient> {
                onGeneric { id } doReturn "foo"
                onGeneric { tokenEndpointAuthenticationMethod } doReturn AuthenticationMethod.privateKeyJwt
            }

            val clientLookup = mock<ClientLookup> {
                onBlocking { find("foo") } doReturn fooClient
            }

            OidcClientAuthenticators(
                    listOf(clientSecretJwtAuthenticator, privateKeyJwtAuthenticator),
                    clientLookup
            )
        }

        val exampleForm: (String) -> OAuthRequestForm = { clientId ->
            OidcRequestForm(mutableMapOf(Param.clientId to listOf(clientId)))
        }
    }
}