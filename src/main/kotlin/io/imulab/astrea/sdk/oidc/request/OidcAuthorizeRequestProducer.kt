package io.imulab.astrea.sdk.oidc.request

import io.imulab.astrea.sdk.oauth.assertType
import io.imulab.astrea.sdk.oauth.client.ClientLookup
import io.imulab.astrea.sdk.oauth.request.OAuthAuthorizeRequest
import io.imulab.astrea.sdk.oauth.request.OAuthAuthorizeRequestProducer
import io.imulab.astrea.sdk.oauth.request.OAuthRequest
import io.imulab.astrea.sdk.oauth.request.OAuthRequestForm
import io.imulab.astrea.sdk.oauth.reserved.space
import io.imulab.astrea.sdk.oauth.validation.SpecDefinitionValidator
import io.imulab.astrea.sdk.oidc.claim.ClaimConverter

/**
 * Extension of [OAuthAuthorizeRequestProducer] to produce a [OidcAuthorizeRequest]. This class utilizes
 * [OAuthAuthorizeRequestProducer] to do the basis work and transform built value back to
 * [OidcAuthorizeRequest.Builder].
 */
class OidcAuthorizeRequestProducer(
    lookup: ClientLookup,
    responseTypeValidator: SpecDefinitionValidator,
    private val claimConverter: io.imulab.astrea.sdk.oidc.claim.ClaimConverter
) : OAuthAuthorizeRequestProducer(lookup, responseTypeValidator) {

    override suspend fun produce(form: OAuthRequestForm): OAuthRequest {
        require(form is OidcRequestForm) { "this producer only produces from OidcRequestForm" }
        val oauthRequest = super.produce(form).assertType<OAuthAuthorizeRequest>()

        return OidcAuthorizeRequest.Builder().also { b ->
            oauthRequest.run {
                b.client = client.assertType()
                b.responseTypes.addAll(responseTypes)
                b.redirectUri = redirectUri
                b.scopes.addAll(scopes)
                b.state = state
            }

            form.run {
                b.responseMode = responseMode
                b.nonce = nonce
                b.display = display
                b.prompts.addAll(prompt.split(space).filter { it.isNotBlank() })
                b.maxAge = maxAge.toLongOrNull() ?: 0
                b.uiLocales.addAll(uiLocales.split(space).filter { it.isNotBlank() })
                b.idTokenHint = idTokenHint
                b.loginHint = loginHint
                b.acrValues.addAll(acrValues.split(space).filter { it.isNotBlank() })
                if (claims.isNotEmpty())
                    b.claims = claimConverter.fromJson(claims)
                b.claimsLocales.addAll(claimsLocales.split(space).filter { it.isNotBlank() })
                b.iss = iss
                b.targetLinkUri = targetLinkUri
                b.session = OidcSession()
                b.session.nonce = b.nonce
            }

            if (b.client?.requireAuthTime == true)
                b.claims.requireAuthTime()
        }.build()
    }
}