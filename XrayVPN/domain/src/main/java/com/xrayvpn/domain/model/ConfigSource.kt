package com.xrayvpn.domain.model

/**
 * Represents a configuration source (subscription URL)
 */
data class ConfigSource(
    val id: String,
    val name: String,
    val url: String,
    val isEnabled: Boolean = true,
    val lastUpdate: Long? = null,
    val serverCount: Int = 0,
    val category: SourceCategory = SourceCategory.CUSTOM
)

enum class SourceCategory {
    VLESS_REALITY_WHITE,
    VLESS_BLACK_RUS_MOBILE,
    VLESS_BLACK_RUS,
    SHADOWSOCKS_ALL,
    WHITE_CIDR_RU,
    WHITE_SNI_RU,
    VLESS_UNIVERSAL,
    CUSTOM
}
