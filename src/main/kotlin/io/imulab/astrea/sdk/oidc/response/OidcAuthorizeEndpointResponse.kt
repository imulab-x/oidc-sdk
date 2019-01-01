package io.imulab.astrea.sdk.oidc.response

import io.imulab.astrea.sdk.oauth.response.AuthorizeEndpointResponse
import io.imulab.astrea.sdk.oidc.reserved.OidcParam

class OidcAuthorizeEndpointResponse(
    override var idToken: String = ""
) : AuthorizeEndpointResponse(), IdTokenResponse {

    override val data: Map<String, String>
        get() {
            val m = super.data as MutableMap
            if (idToken.isNotEmpty())
                m[OidcParam.idToken] = idToken
            return m
        }
}