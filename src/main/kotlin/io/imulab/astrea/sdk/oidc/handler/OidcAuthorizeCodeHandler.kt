package io.imulab.astrea.sdk.oidc.handler

import io.imulab.astrea.sdk.oauth.assertType
import io.imulab.astrea.sdk.oauth.error.InvalidScope
import io.imulab.astrea.sdk.oauth.exactly
import io.imulab.astrea.sdk.oauth.handler.AccessRequestHandler
import io.imulab.astrea.sdk.oauth.handler.AuthorizeRequestHandler
import io.imulab.astrea.sdk.oauth.request.OAuthAccessRequest
import io.imulab.astrea.sdk.oauth.request.OAuthAuthorizeRequest
import io.imulab.astrea.sdk.oauth.reserved.GrantType
import io.imulab.astrea.sdk.oauth.reserved.ResponseType
import io.imulab.astrea.sdk.oauth.response.AuthorizeEndpointResponse
import io.imulab.astrea.sdk.oauth.response.TokenEndpointResponse
import io.imulab.astrea.sdk.oidc.request.OidcSession
import io.imulab.astrea.sdk.oidc.request.OidcSessionRepository
import io.imulab.astrea.sdk.oidc.reserved.IdTokenClaim
import io.imulab.astrea.sdk.oidc.reserved.StandardScope
import io.imulab.astrea.sdk.oidc.response.OidcTokenEndpointResponse
import io.imulab.astrea.sdk.oidc.token.IdTokenStrategy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OidcAuthorizeCodeHandler(
    private val idTokenStrategy: IdTokenStrategy,
    private val oidcSessionRepository: OidcSessionRepository
) : AuthorizeRequestHandler, AccessRequestHandler {

    override suspend fun handleAuthorizeRequest(request: OAuthAuthorizeRequest, response: AuthorizeEndpointResponse) {
        if (!request.responseTypes.exactly(ResponseType.code) || request.session !is OidcSession)
            return

        check(response.code.isNotEmpty()) {
            "Upstream handler should have issued authorization code. Was handler misplaced?"
        }

        withContext(Dispatchers.IO) {
            launch {
                oidcSessionRepository.createOidcSession(response.code, request.session.assertType())
            }
        }
    }

    override suspend fun updateSession(request: OAuthAccessRequest) {}

    override suspend fun handleAccessRequest(request: OAuthAccessRequest, response: TokenEndpointResponse) {
        if (!request.grantTypes.exactly(GrantType.authorizationCode) || request.session !is OidcSession)
            return

        check(response is OidcTokenEndpointResponse) {
            "Called should have supplied an OidcTokenEndpointResponse."
        }

        check(response.accessToken.isNotEmpty()) {
            "Upstream handler should have issued access token. Was handler misplaced?"
        }

        val authorizeSession = oidcSessionRepository.getOidcSession(request.code)
        if (!authorizeSession.grantedScopes.contains(StandardScope.openid))
            throw InvalidScope.notGranted(StandardScope.openid)

        request.session.assertType<OidcSession>().idTokenClaims[IdTokenClaim.accessTokenHash] =
                io.imulab.astrea.sdk.oidc.handler.helper.TokenHashHelper.leftMostHash(
                    response.accessToken,
                    request.client.assertType<io.imulab.astrea.sdk.oidc.client.OidcClient>().idTokenSignedResponseAlgorithm
                )
        response.idToken = idTokenStrategy.generateToken(request)
    }
}