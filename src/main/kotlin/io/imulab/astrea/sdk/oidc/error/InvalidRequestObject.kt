package io.imulab.astrea.sdk.oidc.error

import io.imulab.astrea.sdk.oauth.error.OAuthException

// invalid_request_object
// ----------------------
// The request parameter contains an invalid Request Object.
object InvalidRequestObject {
    private const val code = "invalid_request_object"
    private const val status = 400

    val invalid: () -> Throwable =
        { OAuthException(
            io.imulab.astrea.sdk.oidc.error.InvalidRequestObject.status,
            io.imulab.astrea.sdk.oidc.error.InvalidRequestObject.code, "Request object contained invalid data.") }
}