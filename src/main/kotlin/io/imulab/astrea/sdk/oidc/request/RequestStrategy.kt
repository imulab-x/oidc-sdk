package io.imulab.astrea.sdk.oidc.request

import io.imulab.astrea.sdk.oauth.error.OAuthException
import io.imulab.astrea.sdk.oauth.token.JwtSigningAlgorithm
import io.imulab.astrea.sdk.oauth.token.resolvePrivateKey
import io.imulab.astrea.sdk.oidc.client.OidcClient
import io.imulab.astrea.sdk.oidc.jwk.JsonWebKeySetStrategy
import io.imulab.astrea.sdk.oidc.jwk.JwtVerificationKeyResolver
import io.imulab.astrea.sdk.oidc.jwk.mustKeyForJweKeyManagement
import io.imulab.astrea.sdk.oidc.reserved.JweContentEncodingAlgorithm
import io.imulab.astrea.sdk.oidc.reserved.JweKeyManagementAlgorithm
import io.imulab.astrea.sdk.oidc.spi.SimpleHttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jose4j.jwe.JsonWebEncryption
import org.jose4j.jwk.OctetSequenceJsonWebKey
import org.jose4j.jwt.JwtClaims
import org.jose4j.jwt.consumer.JwtConsumerBuilder
import org.jose4j.keys.AesKey
import java.net.URI
import java.security.MessageDigest
import java.time.Duration
import java.time.LocalDateTime
import java.util.*

/**
 * General logic to resolve a request object
 *
 * When request object is provided through the `request` parameter, it is adopted
 * directly.
 *
 * When request object is provided by reference through the `request_uri` parameter, this strategy
 * checks if it matches any pre-registered values at [OidcClient.requestUris]. If it was registered, an
 * attempt is made to find the request object from cache and compare its hash values to ensure it is the
 * intended version. If it wasn't registered, an attempt is made to fetch the request object from the
 * `request_uri`. Upon successful retrieval, hash is calculated and verified and the item is written
 * to cache to avoid roundtrips later.
 *
 * After securing the request object from various sources, this strategy also attempts to decrypt
 * (if [OidcClient.requestObjectEncryptionAlgorithm] is registered) the request object and verify
 * its signature. In the end, if everything goes well, a [JwtClaims] object representing the content
 * of the request object is returned.
 */
open class RequestStrategy(
    private val repository: CachedRequestRepository,
    private val httpClient: SimpleHttpClient,
    private val jsonWebKeySetStrategy: JsonWebKeySetStrategy,
    private val serverContext: io.imulab.astrea.sdk.oidc.discovery.OidcContext,
    private val requestCacheLifespan: Duration? = Duration.ofDays(30)
) {

    private val sha256: MessageDigest = MessageDigest.getInstance("SHA-256")
    private val base64Encoder: Base64.Encoder = Base64.getUrlEncoder().withoutPadding()

    suspend fun resolveRequest(request: String, requestUri: String, client: io.imulab.astrea.sdk.oidc.client.OidcClient): JwtClaims = try {
        doResolveRequest(request, requestUri, client)
    } catch (e: Exception) {
        when (e) {
            is OAuthException -> throw e
            else -> throw io.imulab.astrea.sdk.oidc.error.InvalidRequestObject.invalid()
        }
    }

    protected open suspend fun doResolveRequest(request: String, requestUri: String, client: io.imulab.astrea.sdk.oidc.client.OidcClient): JwtClaims {
        if (request.isNotEmpty()) {
            check(requestUri.isEmpty()) {
                "request and request_uri cannot be used at the same time."
            }

            return processRequest(request, client)
        }

        if (requestUri.isNotEmpty()) {
            if (!client.requestUris.contains(requestUri))
                throw io.imulab.astrea.sdk.oidc.error.InvalidRequestUri.rouge()
            else if (requestUri.length > 512)
                throw io.imulab.astrea.sdk.oidc.error.InvalidRequestUri.tooLong()

            val cachedRequest = fetchFromRepository(requestUri)
            if (cachedRequest != null)
                return processRequest(request = cachedRequest, client = client)

            return processRequest(
                request = fetchFromRemote(requestUri, client),
                client = client
            )
        }

        return JwtClaims()
    }

    private suspend fun fetchFromRepository(requestUri: String): String? {
        // sanitize request_uri, separate fragment if any
        val fragment = URI(requestUri).fragment
        val cacheId = if (fragment.isEmpty()) requestUri else {
            requestUri.split("#")[0]
        }

        val cached = repository.find(cacheId) ?: return null

        if (cached.hasExpired()) {
            withContext(Dispatchers.IO) {
                launch { repository.evict(cacheId) }
            }
            return null
        }

        if (fragment.isNotEmpty() && !fragment.equals(cached.hash, ignoreCase = true)) {
            withContext(Dispatchers.IO) {
                launch { repository.evict(cacheId) }
            }
            return null
        }

        return cached.request
    }

    private suspend fun fetchFromRemote(requestUri: String, client: io.imulab.astrea.sdk.oidc.client.OidcClient): String {
        val request = withContext(Dispatchers.IO) {
            run {
                httpClient.get(requestUri).let { httpResponse ->
                    when (httpResponse.status()) {
                        200 -> httpResponse.body()
                        else -> throw io.imulab.astrea.sdk.oidc.error.InvalidRequestUri.none200(httpResponse.status())
                    }
                }
            }
        }

        if (request.isEmpty())
            throw io.imulab.astrea.sdk.oidc.error.InvalidRequestUri.invalid()

        val fragment = URI(requestUri).fragment
        val cacheId = if (fragment.isEmpty()) requestUri else {
            requestUri.split("#")[0]
        }

        val hash = base64Encoder.encodeToString(sha256.digest(request.toByteArray()))
        if (fragment.isNotEmpty() && !fragment.equals(hash, ignoreCase = true))
            throw io.imulab.astrea.sdk.oidc.error.InvalidRequestUri.badHash()

        withContext(Dispatchers.IO) {
            launch {
                repository.write(
                    CachedRequest(
                        requestUri = cacheId,
                        request = request,
                        hash = hash,
                        expiry = if (requestCacheLifespan == null) null else
                            LocalDateTime.now().plus(requestCacheLifespan)
                    )
                )
            }
        }

        return request
    }

    protected suspend fun processRequest(request: String, client: io.imulab.astrea.sdk.oidc.client.OidcClient): JwtClaims =
        verifySignature(
            request = decryptRequest(request, client),
            client = client
        )

    private fun decryptRequest(request: String, client: io.imulab.astrea.sdk.oidc.client.OidcClient): String {
        if (!client.requireRequestObjectEncryption())
            return request

        check(client.requestObjectEncryptionAlgorithm != JweKeyManagementAlgorithm.None) {
            "Client requesting encryption must not specify none as key management algorithm"
        }
        check(client.requestObjectEncryptionEncoding != JweContentEncodingAlgorithm.None) {
            "Client requesting encryption must not specify none as content encoding algorithm"
        }

        val jwk = client.requestObjectEncryptionAlgorithm.let { alg ->
            if (alg.isSymmetric)
                OctetSequenceJsonWebKey(AesKey(client.secret))
            else
                serverContext.masterJsonWebKeySet.mustKeyForJweKeyManagement(alg)
        }

        return JsonWebEncryption().also {
            it.setAlgorithmConstraints(client.requestObjectEncryptionAlgorithm.whitelisted())
            it.setContentEncryptionAlgorithmConstraints(client.requestObjectEncryptionEncoding.whitelisted())
            it.compactSerialization = request
            it.key = jwk.resolvePrivateKey()
        }.plaintextString
    }

    private suspend fun verifySignature(request: String, client: io.imulab.astrea.sdk.oidc.client.OidcClient): JwtClaims {
        return when (client.requestObjectSigningAlgorithm) {
            JwtSigningAlgorithm.None -> {
                JwtConsumerBuilder()
                    .setRequireJwtId()
                    .setJwsAlgorithmConstraints(client.requestObjectSigningAlgorithm.whitelisted())
                    .setSkipVerificationKeyResolutionOnNone()
                    .setDisableRequireSignature()
                    .setExpectedAudience(serverContext.issuerUrl)
                    .build()
                    .processToClaims(request)
            }
            else -> {
                val jwks = jsonWebKeySetStrategy.resolveKeySet(client)
                JwtConsumerBuilder()
                    .setRequireJwtId()
                    .setJwsAlgorithmConstraints(client.requestObjectSigningAlgorithm.whitelisted())
                    .setVerificationKeyResolver(
                        JwtVerificationKeyResolver(jwks, client.requestObjectSigningAlgorithm)
                    )
                    .setExpectedIssuer(null)
                    .setExpectedAudience(serverContext.issuerUrl)
                    .build()
                    .processToClaims(request)
            }
        }
    }
}