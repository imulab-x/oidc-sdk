package io.imulab.astrea.sdk.oidc.validation

import io.imulab.astrea.sdk.oauth.error.UnsupportedGrantType
import io.imulab.astrea.sdk.oauth.error.UnsupportedResponseType
import io.imulab.astrea.sdk.oauth.ifNotNullOrEmpty
import io.imulab.astrea.sdk.oauth.request.OAuthAccessRequest
import io.imulab.astrea.sdk.oauth.request.OAuthRequest
import io.imulab.astrea.sdk.oauth.validation.OAuthRequestValidation
import io.imulab.astrea.sdk.oidc.discovery.Discovery
import io.imulab.astrea.sdk.oidc.request.OidcAuthorizeRequest
import io.imulab.astrea.sdk.oidc.reserved.OidcParam

/**
 * Validates if the incoming parameters are actually supported by the server. Support information is supplied through
 * OidcConfig [Discovery] configuration.
 */
class SupportValidator(private val discovery: io.imulab.astrea.sdk.oidc.discovery.Discovery) : OAuthRequestValidation {
    override fun validate(request: OAuthRequest) {
        when (request) {
            is OidcAuthorizeRequest -> validate(request)
            is OAuthAccessRequest -> validate(request)
        }
    }

    private fun validate(request: OidcAuthorizeRequest) {
        request.responseTypes
            .find { !discovery.responseTypesSupported.contains(it) }
            .ifNotNullOrEmpty { throw UnsupportedResponseType.unsupported(it) }

        request.acrValues
            .find { !discovery.acrValuesSupported.contains(it) }
            .ifNotNullOrEmpty { throw io.imulab.astrea.sdk.oidc.error.RequestNotSupported.unsupported(OidcParam.acrValues) }

        request.claimsLocales
            .find { !discovery.claimsLocalesSupported.contains(it) }
            .ifNotNullOrEmpty { throw io.imulab.astrea.sdk.oidc.error.RequestNotSupported.unsupported(OidcParam.claimsLocales) }

        request.uiLocales
            .find { !discovery.uiLocalesSupported.contains(it) }
            .ifNotNullOrEmpty { throw io.imulab.astrea.sdk.oidc.error.RequestNotSupported.unsupported(OidcParam.uiLocales) }

        if (request.responseMode.isNotEmpty() && !discovery.responseModeSupported.contains(request.responseMode))
            throw io.imulab.astrea.sdk.oidc.error.RequestNotSupported.unsupported(OidcParam.responseMode)

        if (request.display.isNotEmpty() && !discovery.displayValuesSupported.contains(request.display))
            throw io.imulab.astrea.sdk.oidc.error.RequestNotSupported.unsupported(OidcParam.display)

        if (request.claims.isNotEmpty() && !discovery.claimsParameterSupported)
            throw io.imulab.astrea.sdk.oidc.error.RequestNotSupported.unsupported(OidcParam.claims)
    }

    private fun validate(request: OAuthAccessRequest) {
        request.grantTypes.find { !discovery.grantTypesSupported.contains(it) }.let { unsupported ->
            if (unsupported != null)
                throw UnsupportedGrantType.unsupported(unsupported)
        }
    }
}
