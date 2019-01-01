package io.imulab.astrea.sdk.oidc.validation

import io.imulab.astrea.sdk.oauth.validation.SpecDefinitionValidator
import io.imulab.astrea.sdk.oidc.reserved.JweContentEncodingAlgorithm

/**
 * Validates the JWE encryption algorithm. The universe is everything specified in [JweContentEncodingAlgorithm].
 */
object EncryptionEncodingValidator: SpecDefinitionValidator {
    override fun validate(value: String): String {
        if (!JweContentEncodingAlgorithm.values().map { it.spec }.contains(value))
            throw IllegalArgumentException("$value is not a valid content encoding algorithm.")
        return value
    }
}