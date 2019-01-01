package io.imulab.astrea.sdk.oidc.validation

import io.imulab.astrea.sdk.oauth.validation.SpecDefinitionValidator
import io.imulab.astrea.sdk.oidc.reserved.JweKeyManagementAlgorithm

/**
 * Validates the JWE encryption algorithm. The universe is everything specified in [JweKeyManagementAlgorithm].
 */
object EncryptionAlgorithmValidator: SpecDefinitionValidator {
    override fun validate(value: String): String {
        if (!JweKeyManagementAlgorithm.values().map { it.spec }.contains(value))
            throw IllegalArgumentException("$value is not a valid key management encryption algorithm.")
        return value
    }
}