package io.imulab.astrea.sdk.oidc.request

import io.kotlintest.matchers.collections.shouldContain
import io.kotlintest.matchers.collections.shouldContainAll
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.kotlintest.specs.BehaviorSpec
import java.time.LocalDateTime

class OidcSessionTest : BehaviorSpec({

    Given("Two oidc sessions") {
        val s1 = OidcSession(
                subject = "",
                obfuscatedSubject = "",
                acrValues = mutableListOf(),
                idTokenClaims = mutableMapOf("a" to "1")
        )
        val s2 = OidcSession(
                subject = "foo",
                obfuscatedSubject = "oof",
                authTime = LocalDateTime.now(),
                nonce = "12345678",
                acrValues = mutableListOf("gold"),
                idTokenClaims = mutableMapOf("b" to "1")
        )

        When("Merged") {
            s1.merge(s2)

            Then("The merger should contain subject") {
                s1.subject shouldBe "foo"
            }

            Then("The merger should contain obs subject") {
                s1.obfuscatedSubject shouldBe "oof"
            }

            Then("The merger should contain auth time") {
                s1.authTime shouldNotBe null
            }

            Then("The merger should contain nonce") {
                s1.nonce shouldBe "12345678"
            }

            Then("The merger should contain all acr values") {
                s1.acrValues shouldContain "gold"
            }

            Then("The merger should contain all id token claims") {
                s1.idTokenClaims.keys shouldContainAll setOf("a", "b")
            }
        }
    }
})