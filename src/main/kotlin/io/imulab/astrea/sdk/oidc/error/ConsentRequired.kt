package io.imulab.astrea.sdk.oidc.error

import io.imulab.astrea.sdk.oauth.error.OAuthException

// consent_required
// ----------------
// The Authorization Server requires End-User consent. This error
// MAY be returned when the prompt parameter value in the Authentication
// Request is none, but the Authentication Request cannot be completed
// without displaying a user interface for End-User consent.
object ConsentRequired {
    private const val code = "consent_required"
    private const val status = 400

    val nonePrompt: () -> Throwable =
        {
            OAuthException(
                io.imulab.astrea.sdk.oidc.error.ConsentRequired.status,
                io.imulab.astrea.sdk.oidc.error.ConsentRequired.code,
                "Server needs to prompt user for consent, but prompt <none> was specified."
            )
        }
}