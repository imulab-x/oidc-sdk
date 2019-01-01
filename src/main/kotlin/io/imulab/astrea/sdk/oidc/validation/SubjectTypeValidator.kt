package io.imulab.astrea.sdk.oidc.validation

import io.imulab.astrea.sdk.oauth.validation.SpecDefinitionValidator
import io.imulab.astrea.sdk.oidc.reserved.SubjectType

/**
 * Validates subject type values. The universe is `{public, pairwise}`.
 */
object SubjectTypeValidator : SpecDefinitionValidator {
    override fun validate(value: String): String {
        return when(value) {
            SubjectType.public, SubjectType.pairwise -> value
            else -> throw IllegalArgumentException("$value is not a valid subject type.")
        }
    }
}