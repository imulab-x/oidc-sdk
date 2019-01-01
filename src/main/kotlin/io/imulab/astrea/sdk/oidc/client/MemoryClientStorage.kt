package io.imulab.astrea.sdk.oidc.client

import io.imulab.astrea.sdk.oauth.client.ClientLookup
import io.imulab.astrea.sdk.oauth.client.OAuthClient
import io.imulab.astrea.sdk.oauth.error.InvalidClient

/**
 * Memory implementation of [ClientLookup].
 */
class MemoryClientStorage(private val database: MutableMap<String, io.imulab.astrea.sdk.oidc.client.OidcClient> = mutableMapOf()) : ClientLookup {

    override suspend fun find(identifier: String): OAuthClient =
            database[identifier] ?: throw InvalidClient.unknown()
}