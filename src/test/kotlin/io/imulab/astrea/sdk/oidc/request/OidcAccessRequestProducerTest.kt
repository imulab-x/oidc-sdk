package io.imulab.astrea.sdk.oidc.request

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import io.imulab.astrea.sdk.oauth.assertType
import io.imulab.astrea.sdk.oauth.client.OAuthClient
import io.imulab.astrea.sdk.oauth.client.authn.ClientAuthenticator
import io.imulab.astrea.sdk.oauth.client.authn.ClientAuthenticators
import io.imulab.astrea.sdk.oauth.request.OAuthAccessRequest
import io.imulab.astrea.sdk.oauth.request.OAuthRequestForm
import io.imulab.astrea.sdk.oauth.reserved.GrantType
import io.imulab.astrea.sdk.oauth.reserved.Param
import io.imulab.astrea.sdk.oauth.validation.OAuthGrantTypeValidator
import io.imulab.astrea.sdk.oidc.client.OidcClient
import io.kotlintest.matchers.collections.shouldContain
import io.kotlintest.matchers.collections.shouldContainAll
import io.kotlintest.should
import io.kotlintest.shouldBe
import io.kotlintest.specs.BehaviorSpec
import kotlinx.coroutines.runBlocking

class OidcAccessRequestProducerTest : BehaviorSpec({

    Given("An access request form") {
        val producer = exampleProducer()

        When("Produced an access request") {
            val form = OidcRequestForm(mutableMapOf(
                    Param.clientId to listOf("foo"),
                    Param.code to listOf("some-code"),
                    Param.grantType to listOf("client_credentials"),
                    Param.scope to listOf("foo bar"),
                    Param.redirectUri to listOf("app://link")
            ))
            val request = runBlocking { producer.produce(form) }

            Then("Request is an access request") {
                request should { it is OAuthAccessRequest }
            }

            Then("Request should have an oidc session") {
                request.session should { it is OidcSession }
            }

            Then("Request should parsed all parameters") {
                request.assertType<OAuthAccessRequest>().run {
                    client.id shouldBe  "foo"
                    code shouldBe "some-code"
                    grantTypes shouldContain GrantType.clientCredentials
                    scopes shouldContainAll setOf("foo", "bar")
                    redirectUri shouldBe "app://link"
                }
            }
        }
    }

}) {
    companion object {
        val exampleProducer: () -> OidcAccessRequestProducer = {
            val client = mock<OidcClient> {
                onGeneric { id } doReturn "foo"
            }

            val authenticator = ClientAuthenticators(authenticators = listOf(
                    object : ClientAuthenticator {
                        override suspend fun authenticate(form: OAuthRequestForm): OAuthClient = client
                        override fun supports(method: String): Boolean = true
                    }
            ))

            OidcAccessRequestProducer(
                    grantTypeValidator = OAuthGrantTypeValidator,
                    clientAuthenticators = authenticator
            )
        }
    }
}