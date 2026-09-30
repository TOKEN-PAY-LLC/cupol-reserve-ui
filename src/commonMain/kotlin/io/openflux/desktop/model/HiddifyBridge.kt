package io.openflux.desktop.model

/** Hiddify imports sing-box JSON; CUPOL Reserve keeps the Yandex carrier behind this local SOCKS5 hop. */
object HiddifyBridge {
    fun profile(socksPort: Int): String {
        require(socksPort in 1..65535) { "Invalid SOCKS5 port" }
        return """{"outbounds":[{"type":"socks","tag":"CUPOL Reserve","server":"127.0.0.1","server_port":$socksPort,"version":"5"}]}"""
    }
}
