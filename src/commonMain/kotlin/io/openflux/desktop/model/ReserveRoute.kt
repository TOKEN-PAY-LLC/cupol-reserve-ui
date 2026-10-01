package io.openflux.desktop.model

import kotlinx.serialization.Serializable

/** Selects carriers within the same encrypted session. */
@Serializable
enum class ReserveRoute {
    Auto, Yandex;

    fun label(language: String = "ru"): String = when (this) {
        Auto -> if (language == "ru") "Обход · Авто" else "Reserve · Auto"
        Yandex -> if (language == "ru") "Обход · Яндекс" else "Reserve · Yandex"
    }

    fun description(language: String = "ru"): String = when (this) {
        Auto -> if (language == "ru") "Автоматическое переключение между доступными каналами."
            else "Automatically switch between available carriers."
        Yandex -> if (language == "ru") "Трафик идёт только через каналы Яндекса."
            else "Send traffic only through Yandex carriers."
    }
}

val TransportType.isYandexCarrier: Boolean
    get() = this == TransportType.VYANDEX || this == TransportType.YANDEX || this == TransportType.BOARDS
