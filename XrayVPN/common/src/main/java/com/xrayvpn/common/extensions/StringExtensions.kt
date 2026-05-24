package com.xrayvpn.common.extensions

import java.net.InetAddress

/**
 * Extract host from URI, handling IPv6 addresses
 */
fun String.idnHost(): String {
    return try {
        val host = this.removePrefix("[").removeSuffix("]")
        InetAddress.getByName(host).hostName ?: host
    } catch (e: Exception) {
        this.removePrefix("[").removeSuffix("]")
    }
}

/**
 * Check if string is not null and not empty
 */
fun String?.isNotNullEmpty(): Boolean = !this.isNullOrBlank()

/**
 * Return null if string is blank, otherwise return the string
 */
fun String.nullIfBlank(): String? = if (this.isBlank()) null else this

/**
 * URL encode a string
 */
fun String.urlEncode(): String = java.net.URLEncoder.encode(this, "UTF-8")

/**
 * URL decode a string
 */
fun String.urlDecode(): String = java.net.URLDecoder.decode(this, "UTF-8")

/**
 * Get IPv6 address format
 */
fun String?.toIpv6Address(): String {
    if (this.isNullOrBlank()) return ""
    return if (this.contains(":") && !this.startsWith("[")) {
        "[$this]"
    } else {
        this
    }
}
