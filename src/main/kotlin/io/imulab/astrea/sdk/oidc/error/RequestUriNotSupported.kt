package io.imulab.astrea.sdk.oidc.error

import io.imulab.astrea.sdk.oauth.error.OAuthException

// request_uri_not_supported
// -------------------------
// The OP does not support use of the request_uri parameter defined in
// Section 6 (https://openid.net/specs/openid-connect-core-1_0.html#JWTRequests).
object RequestUriNotSupported {
    private const val code = "request_uri_not_supported"
    private const val status = 400

    val unsupported: () -> Throwable =
        { OAuthException(
            io.imulab.astrea.sdk.oidc.error.RequestUriNotSupported.status,
            io.imulab.astrea.sdk.oidc.error.RequestUriNotSupported.code, "The use of request_uri parameter is not supported.") }
}