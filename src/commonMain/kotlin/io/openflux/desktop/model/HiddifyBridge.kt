package io.openflux.desktop.model

/** Hiddify imports sing-box JSON; CUPOL Reserve keeps the Yandex carrier behind this local SOCKS5 hop. */
object HiddifyBridge {
    fun profile(socksPort: Int): String {
        require(socksPort in 1..65535) { "Invalid SOCKS5 port" }
        return """{"outbounds":[{"type":"socks","tag":"Обход","server":"127.0.0.1","server_port":$socksPort,"version":"5"}]}"""
    }

    /** Import as a separate SOCKS node in Streisand while on the same LAN. */
    fun streisandLink(host: String, socksPort: Int, lanPassword: String): String {
        require(socksPort in 1..65535) { "Invalid SOCKS5 port" }
        require(IPV4.matches(host) && host.split('.').all { it.toInt() in 0..255 }) {
            "Enter the phone's LAN IPv4 address"
        }
        require(CREDENTIAL.matches(lanPassword)) { "Invalid SOCKS5 credential" }
        return "socks://cupol:$lanPassword@$host:$socksPort#%D0%9E%D0%B1%D1%85%D0%BE%D0%B4"
    }

    private val CREDENTIAL = Regex("[0-9a-fA-F]{64}")
    private val IPV4 = Regex("(?:[0-9]{1,3}\\.){3}[0-9]{1,3}")
}
