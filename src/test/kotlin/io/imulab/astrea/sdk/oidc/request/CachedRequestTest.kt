package io.imulab.astrea.sdk.oidc.request

import io.kotlintest.shouldBe
import io.kotlintest.specs.ExpectSpec
import java.time.LocalDateTime

class CachedRequestTest : ExpectSpec({

    context("A cached request with expiration in the future") {
        val request = CachedRequest(
                requestUri = "",
                request = "",
                expiry = LocalDateTime.now().plusDays(1)
        )

        expect("it has not expired") {
            request.hasExpired() shouldBe false
        }
    }

    context("A cached request with expiration in the past") {
        val request = CachedRequest(
                requestUri = "",
                request = "",
                expiry = LocalDateTime.now().minusSeconds(10)
        )

        expect("it has expired") {
            request.hasExpired() shouldBe true
        }
    }

    context("A cached request with no expiration") {
        val request = CachedRequest(
                requestUri = "",
                request = ""
        )

        expect("it has not expired") {
            request.hasExpired() shouldBe false
        }
    }
})