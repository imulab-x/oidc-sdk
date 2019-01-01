package io.imulab.astrea.sdk.oidc.handler

import io.imulab.astrea.sdk.oauth.assertType
import io.imulab.astrea.sdk.oauth.exactly
import io.imulab.astrea.sdk.oauth.handler.AuthorizeRequestHandler
import io.imulab.astrea.sdk.oauth.handler.helper.AccessTokenHelper
import io.imulab.astrea.sdk.oauth.request.OAuthAuthorizeRequest
import io.imulab.astrea.sdk.oauth.reserved.GrantType
import io.imulab.astrea.sdk.oauth.response.AuthorizeEndpointResponse
import io.imulab.astrea.sdk.oidc.request.OidcSession
import io.imulab.astrea.sdk.oidc.reserved.IdTokenClaim
import io.imulab.astrea.sdk.oidc.reserved.ResponseType
import io.imulab.astrea.sdk.oidc.reserved.StandardScope
import io.imulab.astrea.sdk.oidc.response.OidcAuthorizeEndpointResponse
import io.imulab.astrea.sdk.oidc.token.IdTokenStrategy

class OidcImplicitHandler(
    private val accessTokenHelper: AccessTokenHelper,
    private val idTokenStrategy: IdTokenStrategy
) : AuthorizeRequestHandler {

    override suspend fun handleAuthorizeRequest(request: OAuthAuthorizeRequest, response: AuthorizeEndpointResponse) {
        if (!request.shouldBeHandled())
            return

        check(response is OidcAuthorizeEndpointResponse) {
            "Caller should have supplied an OidcAuthorizeEndpointResponse"
        }

        request.client.mustGrantType(GrantType.implicit)

        if (request.state.isNotEmpty())
            response.state = request.state

        if (request.responseTypes.contains(io.imulab.astrea.sdk.oauth.reserved.ResponseType.token)) {
            if (response.accessToken.isEmpty())
                accessTokenHelper.createAccessToken(request, response).join()

            request.session.assertType<OidcSession>().idTokenClaims[IdTokenClaim.accessTokenHash] =
                    io.imulab.astrea.sdk.oidc.handler.helper.TokenHashHelper.leftMostHash(
                        response.accessToken,
                        request.client.assertType<io.imulab.astrea.sdk.oidc.client.OidcClient>().idTokenSignedResponseAlgorithm
                    )
            response.handledResponseTypes.add(io.imulab.astrea.sdk.oauth.reserved.ResponseType.token)
        }

        response.idToken = idTokenStrategy.generateToken(request)
        response.handledResponseTypes.add(ResponseType.idToken)
    }

    private fun OAuthAuthorizeRequest.shouldBeHandled(): Boolean {
        if (!session.grantedScopes.contains(StandardScope.openid))
            return false
        else if (session !is OidcSession)
            return false

        return when {
            responseTypes.exactly(ResponseType.idToken) -> true
            responseTypes.containsAll(
                listOf(
                    ResponseType.idToken,
                    io.imulab.astrea.sdk.oauth.reserved.ResponseType.token
                )
            ) -> true
            else -> false
        }
    }
}