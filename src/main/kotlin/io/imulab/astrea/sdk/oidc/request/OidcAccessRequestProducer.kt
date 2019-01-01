package io.imulab.astrea.sdk.oidc.request

import io.imulab.astrea.sdk.oauth.client.authn.ClientAuthenticators
import io.imulab.astrea.sdk.oauth.request.OAuthAccessRequest
import io.imulab.astrea.sdk.oauth.request.OAuthAccessRequestProducer
import io.imulab.astrea.sdk.oauth.request.OAuthRequestForm
import io.imulab.astrea.sdk.oauth.validation.SpecDefinitionValidator

class OidcAccessRequestProducer(
    grantTypeValidator: SpecDefinitionValidator,
    clientAuthenticators: ClientAuthenticators
) : OAuthAccessRequestProducer(grantTypeValidator, clientAuthenticators) {

    override suspend fun builder(form: OAuthRequestForm): OAuthAccessRequest.Builder {
        return super.builder(form).apply { session = OidcSession() }
    }
}