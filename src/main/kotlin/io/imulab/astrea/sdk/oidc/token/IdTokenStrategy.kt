package io.imulab.astrea.sdk.oidc.token

import io.imulab.astrea.sdk.oauth.request.OAuthRequest

/**
 * Strategy to generate an id token.
 */
interface IdTokenStrategy {

    /**
     * Generate a new id token.
     */
    suspend fun generateToken(request: OAuthRequest): String
}