package io.imulab.astrea.sdk.oidc.jwk

import io.imulab.astrea.sdk.oauth.error.ServerError
import io.imulab.astrea.sdk.oidc.reserved.JweKeyManagementAlgorithm
import org.jose4j.jwk.JsonWebKey
import org.jose4j.jwk.JsonWebKeySet
import org.jose4j.jwk.Use

fun JsonWebKeySet.mustKeyForJweKeyManagement(keyAlg: JweKeyManagementAlgorithm): JsonWebKey =
    this.findJsonWebKey(null, null, Use.ENCRYPTION, keyAlg.algorithmIdentifier)
        ?: throw ServerError.internal("Cannot find key for managing ${keyAlg.spec}.")

fun JsonWebKeySet.mustKeyWithId(keyId: String): JsonWebKey =
    this.findJsonWebKey(keyId, null, null, null)
        ?: throw ServerError.internal("Cannot find key with id $keyId.")