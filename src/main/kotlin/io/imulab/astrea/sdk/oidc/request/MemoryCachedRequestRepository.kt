package io.imulab.astrea.sdk.oidc.request

/**
 * Memory implementation of [CachedRequestRepository].
 */
class MemoryCachedRequestRepository : CachedRequestRepository {

    private val database = mutableMapOf<String, CachedRequest>()

    override suspend fun write(request: CachedRequest) {
        database[request.requestUri] = request
    }

    override suspend fun find(requestUri: String): CachedRequest? = database[requestUri]

    override suspend fun evict(requestUri: String) {
        database.remove(requestUri)
    }
}