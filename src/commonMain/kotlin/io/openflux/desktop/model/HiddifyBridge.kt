package io.openflux.desktop.model

import kotlinx.serialization.json.JsonPrimitive

/** Hiddify imports sing-box JSON; CUPOL Reserve keeps the Yandex carrier behind this local SOCKS5 hop. */
object HiddifyBridge {
    fun profile(socksPort: Int, title: String = "Обход"): String {
        require(socksPort in 1..65535) { "Invalid SOCKS5 port" }
        return """{"outbounds":[{"type":"socks","tag":${JsonPrimitive(title)},"server":"127.0.0.1","server_port":$socksPort,"version":"5"}]}"""
    }

    /** Import as a separate SOCKS node in Streisand while on the same LAN. */
    fun streisandLink(host: String, socksPort: Int, lanPassword: String, title: String = "Обход"): String {
        require(socksPort in 1..65535) { "Invalid SOCKS5 port" }
        require(IPV4.matches(host) && host.split('.').all { it.toInt() in 0..255 }) {
            "Enter the phone's LAN IPv4 address"
        }
        require(CREDENTIAL.matches(lanPassword)) { "Invalid SOCKS5 credential" }
        val fragment = title.encodeToByteArray().joinToString("") {
            "%" + (it.toInt() and 255).toString(16).uppercase().padStart(2, '0')
        }
        return "socks://cupol:$lanPassword@$host:$socksPort#$fragment"
    }

    private val CREDENTIAL = Regex("[0-9a-fA-F]{64}")
    private val IPV4 = Regex("(?:[0-9]{1,3}\\.){3}[0-9]{1,3}")
}
