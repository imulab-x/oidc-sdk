package io.imulab.astrea.sdk.oidc.request

import io.imulab.astrea.sdk.oauth.request.OAuthSession
import java.time.LocalDateTime

/**
 * Open ID Connect User Session.
 */
open class OidcSession(
    subject: String = "",
    var obfuscatedSubject: String = "",
    var authTime: LocalDateTime? = null,
    var acrValues: MutableList<String> = mutableListOf(),
    // authorize request should set this value
    var nonce: String = "",
    var idTokenClaims: MutableMap<String, Any> = mutableMapOf()
) : OAuthSession(subject) {

    override fun merge(another: OAuthSession) {
        super.merge(another)
        if (another is OidcSession) {
            if (obfuscatedSubject.isEmpty())
                obfuscatedSubject = another.obfuscatedSubject
            if (authTime == null)
                authTime = another.authTime
            acrValues.addAll(another.acrValues)
            if (nonce.isEmpty())
                nonce = another.nonce
            idTokenClaims.putAll(another.idTokenClaims)
        }
    }
}