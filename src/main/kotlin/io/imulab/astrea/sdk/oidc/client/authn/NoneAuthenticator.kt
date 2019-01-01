package io.imulab.astrea.sdk.oidc.client.authn

import io.imulab.astrea.sdk.oauth.client.ClientLookup
import io.imulab.astrea.sdk.oauth.client.OAuthClient
import io.imulab.astrea.sdk.oauth.client.authn.ClientAuthenticator
import io.imulab.astrea.sdk.oauth.error.InvalidClient
import io.imulab.astrea.sdk.oauth.request.OAuthRequestForm
import io.imulab.astrea.sdk.oauth.reserved.ClientType
import io.imulab.astrea.sdk.oauth.reserved.GrantType
import io.imulab.astrea.sdk.oidc.reserved.AuthenticationMethod

/**
 * Implementation of [ClientAuthenticator] that supports [AuthenticationMethod.none]. When the client is either a
 * public client or a client that does not use the token endpoint (i.e. uses only implicit flow), it can use
 * `none` as an authentication method and passes authentication without providing any credentials.
 */
class NoneAuthenticator(
    private val clientLookup: ClientLookup
) : ClientAuthenticator {

    override fun supports(method: String): Boolean = method == AuthenticationMethod.none

    override suspend fun authenticate(form: OAuthRequestForm): OAuthClient {
        val client = clientLookup.find(form.clientId)
        if (client.type == ClientType.public)
            return client
        else if (client.grantTypes.contains(GrantType.implicit) && client.grantTypes.size == 1)
            return client

        throw InvalidClient.unauthorized(AuthenticationMethod.none)
    }
}