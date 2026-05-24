package com.xrayvpn.common.utils

import android.util.Base64
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.UUID

/**
 * Base64 encode/decode utilities
 */
object Base64Util {
    
    /**
     * Decode base64 string with padding fix
     */
    fun decode(str: String): String {
        return try {
            val padded = addBase64Padding(str)
            String(Base64.decode(padded, Base64.NO_WRAP), StandardCharsets.UTF_8)
        } catch (e: Exception) {
            str
        }
    }
    
    /**
     * Encode string to base64
     */
    fun encode(str: String): String {
        return Base64.encodeToString(str.toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP)
    }
    
    /**
     * Add padding to base64 string if needed
     */
    private fun addBase64Padding(str: String): String {
        var s = str.replace("-", "+").replace("_", "/")
        while (s.length % 4 != 0) {
            s += "="
        }
        return s
    }
}

/**
 * URL encode/decode utilities
 */
object UrlUtil {
    
    /**
     * URL encode a string
     */
    fun encode(str: String): String {
        return URLEncoder.encode(str, "UTF-8")
    }
    
    /**
     * URL decode a string
     */
    fun decode(str: String): String {
        return try {
            URLDecoder.decode(str, "UTF-8")
        } catch (e: Exception) {
            str
        }
    }
}

/**
 * UUID utilities
 */
object UuidUtil {
    
    /**
     * Generate a random UUID string
     */
    fun generate(): String {
        return UUID.randomUUID().toString()
    }
}

/**
 * General utility functions
 */
object CommonUtils {
    
    /**
     * Fix illegal URL characters
     */
    fun fixIllegalUrl(str: String): String {
        return str.replace(" ", "%20")
            .replace("|", "%7C")
            .replace("#", "%23")
    }
    
    /**
     * Check if string is a valid URL
     */
    fun isValidUrl(url: String?): Boolean {
        if (url.isNullOrBlank()) return false
        return try {
            url.startsWith("http://") || url.startsWith("https://") || 
                url.startsWith("socks://") || url.startsWith("ss://") ||
                url.startsWith("vmess://") || url.startsWith("vless://") ||
                url.startsWith("trojan://") || url.startsWith("hysteria2://") ||
                url.startsWith("hy2://") || url.startsWith("wg://")
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Get IPv6 address format
     */
    fun getIpv6Address(address: String?): String {
        if (address.isNullOrBlank()) return ""
        return if (address.contains(":") && !address.startsWith("[")) {
            "[$address]"
        } else {
            address
        }
    }
}
