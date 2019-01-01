package io.imulab.astrea.sdk.oidc.claim

import io.kotlintest.matchers.collections.shouldContain
import io.kotlintest.matchers.collections.shouldContainAll
import io.kotlintest.shouldBe
import io.kotlintest.specs.FeatureSpec

class ClaimTest : FeatureSpec({

    feature("Example claim object") {
        val example = Claim(name = "foo", essential = true, source = "id_token", values = listOf("bar"))

        scenario("should be essential") {
            example.essential shouldBe true
        }

        scenario("name should be foo") {
            example.name shouldBe "foo"
        }

        scenario("source should be id_token") {
            example.source shouldBe "id_token"
        }

        scenario("values should contain bar") {
            example.values shouldContain "bar"
        }

        scenario("map representation should contain all entries") {
            example.toMap().keys shouldContainAll setOf("name", "essential", "source", "values")
        }
    }
})