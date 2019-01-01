package io.imulab.astrea.sdk.oidc.request

import io.imulab.astrea.sdk.oauth.error.InvalidGrant

class MemoryOidcSessionRepository : OidcSessionRepository {

    private val db = mutableMapOf<String, OidcSession>()

    override suspend fun createOidcSession(authorizeCode: String, session: OidcSession) {
        db[authorizeCode] = session
    }

    override suspend fun getOidcSession(authorizeCode: String): OidcSession {
        return db[authorizeCode] ?: throw InvalidGrant.invalid()
    }

    override suspend fun deleteOidcSession(authorizeCode: String) {
        db.remove(authorizeCode)
    }
}