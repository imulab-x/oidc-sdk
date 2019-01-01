package io.imulab.astrea.sdk.oidc.validation

import io.imulab.astrea.sdk.oauth.assertType
import io.imulab.astrea.sdk.oauth.error.InvalidRequest
import io.imulab.astrea.sdk.oauth.request.OAuthRequest
import io.imulab.astrea.sdk.oauth.validation.OAuthRequestValidation
import io.imulab.astrea.sdk.oidc.request.OidcAuthorizeRequest
import io.imulab.astrea.sdk.oidc.reserved.OidcParam

/**
 * Validates the `max_age` parameter. It must not be less than 0.
 */
object MaxAgeValidator : OAuthRequestValidation {
    override fun validate(request: OAuthRequest) {
        if (request.assertType<OidcAuthorizeRequest>().maxAge < 0)
            throw InvalidRequest.invalid(OidcParam.maxAge)
    }
}