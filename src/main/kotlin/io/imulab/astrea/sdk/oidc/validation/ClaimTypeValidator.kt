package io.imulab.astrea.sdk.oidc.validation

import io.imulab.astrea.sdk.oauth.validation.SpecDefinitionValidator
import io.imulab.astrea.sdk.oidc.reserved.ClaimType

/**
 * Validates the claim type configuration parameter. The universe is `{normal, aggregated, distributed}`.
 */
object ClaimTypeValidator: SpecDefinitionValidator {
    override fun validate(value: String): String {
        return when (value) {
            ClaimType.normal, ClaimType.aggregated, ClaimType.distributed -> value
            else -> throw IllegalArgumentException("$value is not a valid claim type.")
        }
    }
}