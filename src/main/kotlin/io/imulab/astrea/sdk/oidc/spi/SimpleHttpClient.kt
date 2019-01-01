package io.imulab.astrea.sdk.oidc.spi

interface SimpleHttpClient {

    suspend fun get(url: String): HttpResponse
}