package io.imulab.astrea.sdk.oidc.spi

interface HttpResponse {

    fun status(): Int

    fun body(): String
}