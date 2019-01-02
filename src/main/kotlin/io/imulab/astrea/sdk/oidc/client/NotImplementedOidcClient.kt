package io.imulab.astrea.sdk.oidc.client

import io.imulab.astrea.sdk.oauth.token.JwtSigningAlgorithm
import io.imulab.astrea.sdk.oidc.reserved.JweContentEncodingAlgorithm
import io.imulab.astrea.sdk.oidc.reserved.JweKeyManagementAlgorithm

open class NotImplementedOidcClient : OidcClient {

    private val notImplemented: () -> Nothing = { throw NotImplementedError() }

    override val applicationType: String by lazy(notImplemented)
    override val contacts: LinkedHashSet<String> by lazy(notImplemented)
    override val logoUri: String by lazy(notImplemented)
    override val clientUri: String by lazy(notImplemented)
    override val policyUri: String by lazy(notImplemented)
    override val tosUri: String by lazy(notImplemented)
    override val jwksUri: String by lazy(notImplemented)
    override val jwks: String by lazy(notImplemented)
    override val sectorIdentifierUri: String by lazy(notImplemented)
    override val subjectType: String by lazy(notImplemented)
    override val idTokenSignedResponseAlgorithm: JwtSigningAlgorithm by lazy(notImplemented)
    override val idTokenEncryptedResponseAlgorithm: JweKeyManagementAlgorithm by lazy(notImplemented)
    override val idTokenEncryptedResponseEncoding: JweContentEncodingAlgorithm by lazy(notImplemented)
    override val requestObjectSigningAlgorithm: JwtSigningAlgorithm by lazy(notImplemented)
    override val requestObjectEncryptionAlgorithm: JweKeyManagementAlgorithm by lazy(notImplemented)
    override val requestObjectEncryptionEncoding: JweContentEncodingAlgorithm by lazy(notImplemented)
    override val userInfoSignedResponseAlgorithm: JwtSigningAlgorithm by lazy(notImplemented)
    override val userInfoEncryptedResponseAlgorithm: JweKeyManagementAlgorithm by lazy(notImplemented)
    override val userInfoEncryptedResponseEncoding: JweContentEncodingAlgorithm by lazy(notImplemented)
    override val tokenEndpointAuthenticationMethod: String by lazy(notImplemented)
    override val tokenEndpointAuthenticationSigningAlgorithm: JwtSigningAlgorithm by lazy(notImplemented)
    override val defaultMaxAge: Long by lazy(notImplemented)
    override val requireAuthTime: Boolean by lazy(notImplemented)
    override val defaultAcrValues: List<String> by lazy(notImplemented)
    override val initiateLoginUri: String by lazy(notImplemented)
    override val requestUris: List<String> by lazy(notImplemented)
    override val id: String by lazy(notImplemented)
    override val secret: ByteArray by lazy(notImplemented)
    override val name: String by lazy(notImplemented)
    override val type: String by lazy(notImplemented)
    override val redirectUris: Set<String> by lazy(notImplemented)
    override val responseTypes: Set<String> by lazy(notImplemented)
    override val grantTypes: Set<String> by lazy(notImplemented)
    override val scopes: Set<String> by lazy(notImplemented)
}