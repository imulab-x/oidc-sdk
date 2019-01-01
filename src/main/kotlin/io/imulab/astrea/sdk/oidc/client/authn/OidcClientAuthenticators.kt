package io.imulab.astrea.sdk.oidc.client.authn

import io.imulab.astrea.sdk.oauth.assertType
import io.imulab.astrea.sdk.oauth.client.ClientLookup
import io.imulab.astrea.sdk.oauth.client.authn.ClientAuthenticator
import io.imulab.astrea.sdk.oauth.client.authn.ClientAuthenticators

/**
 * Simple override of [ClientAuthenticators] to use the client pre-registered value of token endpoint authentication
 * method as the actual authentication method, instead of a default fixed one determined by the server.
 */
class OidcClientAuthenticators(
    authenticators: List<ClientAuthenticator>,
    private val clientLookup: ClientLookup
) : ClientAuthenticators(
    authenticators = authenticators,
    methodFinder = { f ->
        clientLookup.find(f.clientId)
            .assertType<io.imulab.astrea.sdk.oidc.client.OidcClient>()
            .tokenEndpointAuthenticationMethod
    }
)