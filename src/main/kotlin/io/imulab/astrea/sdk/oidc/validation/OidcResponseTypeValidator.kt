package io.imulab.astrea.sdk.oidc.validation

import io.imulab.astrea.sdk.oauth.assertType
import io.imulab.astrea.sdk.oauth.error.UnauthorizedClient
import io.imulab.astrea.sdk.oauth.error.UnsupportedResponseType
import io.imulab.astrea.sdk.oauth.request.OAuthRequest
import io.imulab.astrea.sdk.oauth.validation.OAuthRequestValidation
import io.imulab.astrea.sdk.oauth.validation.OAuthResponseTypeValidator
import io.imulab.astrea.sdk.oauth.validation.SpecDefinitionValidator
import io.imulab.astrea.sdk.oidc.request.OidcAuthorizeRequest
import io.imulab.astrea.sdk.oidc.reserved.ResponseType

/**
 * Validates the set relation: `response_type = {code, token}`.
 * When in the context of a request, it must be registered/allowed by the client.
 */
object OidcResponseTypeValidator : SpecDefinitionValidator,
    OAuthRequestValidation {
    override fun validate(value: String): String {
        try {
            return OAuthResponseTypeValidator.validate(value)
        } catch (e: Exception) {
            if (value == ResponseType.idToken)
                return value
            throw UnsupportedResponseType.unsupported(value)
        }
    }

    override fun validate(request: OAuthRequest) {
        val ar = request.assertType<OidcAuthorizeRequest>()
        ar.responseTypes.forEach {
            validate(it)
            if (!ar.client.responseTypes.contains(it))
                throw UnauthorizedClient.forbiddenResponseType(it)
        }
    }
}