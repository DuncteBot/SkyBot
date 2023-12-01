package com.dunctebot.dashboard.utils

import java.security.MessageDigest

/**
 * Hashing Utils [https://www.samclarke.com/kotlin-hash-strings/](https://www.samclarke.com/kotlin-hash-strings/)
 * @author Sam Clarke <www.samclarke.com>
 * @license MIT
 */
object HashUtils {
    private const val HEX_CHARS = "0123456789ABCDEF"

    fun sha512(input: String) = hashString("SHA-512", input)

    fun sha256(input: String) = hashString("SHA-256", input)

    fun sha1(input: String) = hashString("SHA-1", input)

    /**
     * Supported algorithms on Android:
     *
     * Algorithm	Supported API Levels
     * MD5          1+
     * SHA-1	    1+
     * SHA-224	    1-8,22+
     * SHA-256	    1+
     * SHA-384	    1+
     * SHA-512	    1+
     */
    private fun hashString(type: String, input: String): String {
        val bytes = MessageDigest
            .getInstance(type)
            .digest(input.toByteArray())

        return buildString(bytes.size * 2) {
            bytes.forEach {
                val i = it.toInt()
                append(HEX_CHARS[i shr 4 and 0x0f])
                append(HEX_CHARS[i and 0x0f])
            }
        }
    }
}
