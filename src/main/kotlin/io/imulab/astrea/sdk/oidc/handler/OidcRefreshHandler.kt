package io.imulab.astrea.sdk.oidc.handler

import io.imulab.astrea.sdk.oauth.assertType
import io.imulab.astrea.sdk.oauth.handler.AccessRequestHandler
import io.imulab.astrea.sdk.oauth.request.OAuthAccessRequest
import io.imulab.astrea.sdk.oauth.reserved.GrantType
import io.imulab.astrea.sdk.oauth.response.TokenEndpointResponse
import io.imulab.astrea.sdk.oidc.request.OidcSession
import io.imulab.astrea.sdk.oidc.reserved.IdTokenClaim
import io.imulab.astrea.sdk.oidc.reserved.StandardScope
import io.imulab.astrea.sdk.oidc.response.OidcTokenEndpointResponse
import io.imulab.astrea.sdk.oidc.token.IdTokenStrategy

class OidcRefreshHandler(private val idTokenStrategy: IdTokenStrategy) : AccessRequestHandler {

    override suspend fun updateSession(request: OAuthAccessRequest) {}

    override suspend fun handleAccessRequest(request: OAuthAccessRequest, response: TokenEndpointResponse) {
        if (!request.shouldBeHandled())
            return

        check(response is OidcTokenEndpointResponse) {
            "Upstream should have supplied an OidcTokenEndpointResponse"
        }

        check(response.accessToken.isNotEmpty()) {
            "Upstream should have generated an access token. Was handler misplaced?"
        }

        request.session.assertType<OidcSession>().idTokenClaims[IdTokenClaim.accessTokenHash] =
                io.imulab.astrea.sdk.oidc.handler.helper.TokenHashHelper.leftMostHash(
                    response.accessToken,
                    request.client.assertType<io.imulab.astrea.sdk.oidc.client.OidcClient>().idTokenSignedResponseAlgorithm
                )
        response.idToken = idTokenStrategy.generateToken(request)
    }

    private fun OAuthAccessRequest.shouldBeHandled(): Boolean =
        grantTypes.contains(GrantType.refreshToken) && session.grantedScopes.contains(StandardScope.openid)
}