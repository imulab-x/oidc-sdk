package io.imulab.astrea.sdk.oidc.validation

import io.imulab.astrea.sdk.oauth.token.JwtSigningAlgorithm
import io.imulab.astrea.sdk.oauth.validation.SpecDefinitionValidator

/**
 * Validates the JWT signing algorithm. The universe is everything specified in [JwtSigningAlgorithm].
 */
object SigningAlgorithmValidator: SpecDefinitionValidator {
    override fun validate(value: String): String {
        if (!JwtSigningAlgorithm.values().map { it.spec }.contains(value))
            throw IllegalArgumentException("$value is not a valid signing algorithm.")
        return value
    }
}