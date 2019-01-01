package io.imulab.astrea.sdk.oidc.error

import io.imulab.astrea.sdk.oauth.error.OAuthException

// login_required
// --------------
// The Authorization Server requires End-User authentication.
// This error MAY be returned when the prompt parameter value in
// the Authentication Request is none, but the Authentication
// Request cannot be completed without displaying a user interface
// for End-User authentication.
object LoginRequired {
    const val code = "login_required"
    private const val status = 400

    val nonePrompt: () -> Throwable =
        {
            OAuthException(
                io.imulab.astrea.sdk.oidc.error.LoginRequired.status,
                io.imulab.astrea.sdk.oidc.error.LoginRequired.code,
                "Server needs to display authentication UI to end user, but prompt <none> was specified."
            )
        }

    val reEntryInVain : () -> Throwable =
        {
            OAuthException(
                io.imulab.astrea.sdk.oidc.error.LoginRequired.status,
                io.imulab.astrea.sdk.oidc.error.LoginRequired.code,
                "Server cannot establish authentication after an active login attempt."
            )
        }

    val error : (String) -> Throwable =
        { reason -> OAuthException(
            io.imulab.astrea.sdk.oidc.error.LoginRequired.status,
            io.imulab.astrea.sdk.oidc.error.LoginRequired.code, reason) }
}