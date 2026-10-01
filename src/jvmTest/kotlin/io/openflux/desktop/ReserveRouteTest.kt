package io.openflux.desktop

import io.openflux.desktop.model.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.URI
import java.net.URLDecoder
import kotlin.test.*

class ReserveRouteTest {
    private val profile = Profile(
        id = "p", name = "Reserve", transport = TransportType.VYANDEX,
        value = "https://docs.example/yandex", secret = "f".repeat(64), session = true,
        extras = listOf(
            ExtraTransport(TransportType.DIRECT, "203.0.113.10:33445", priority = 110),
            ExtraTransport(TransportType.MAILRU, "https://docs.example/mail", priority = 120),
            ExtraTransport(TransportType.YANDEX, "https://docs.example/second", priority = 90),
        ),
    )
    private val paths = CorePaths("C:/key", "C:/conf", "C:/cookies", null)

    @Test fun automaticModeKeepsAllConfiguredCarriers() {
        assertEquals(profile.configuredCarriers, profile.carriers)
        assertEquals("", profile.effectiveSessionContext)
        assertTrue(profile.supportsReserveRoutes)
    }

    @Test fun yandexModeFiltersCarriersAndKeepsOriginalSalt() {
        val forced = profile.copy(reserveRoute = ReserveRoute.Yandex)
        assertEquals(listOf(TransportType.VYANDEX, TransportType.YANDEX), forced.carriers.map { it.type })
        assertEquals(profile.configuredCarriers, forced.configuredCarriers)
        assertEquals("https://docs.example/mail", forced.effectiveSessionContext)
        assertEquals(listOf("vyandex", "yandex"), forced.sessionSpecs().map { it.name })
        assertEquals(profile.carriers, forced.copy(reserveRoute = ReserveRoute.Auto).carriers)
    }

    @Test fun explicitSaltAlwaysSurvivesRouteSelection() {
        assertEquals("original-channel", profile.copy(context = "original-channel", reserveRoute = ReserveRoute.Yandex).effectiveSessionContext)
    }

    @Test fun tiedPrioritiesKeepTheFirstOriginalDocument() {
        val tied = profile.copy(extras = listOf(ExtraTransport(TransportType.YANDEX, "https://docs.example/tied", priority = 100)), reserveRoute = ReserveRoute.Yandex)
        assertEquals(profile.value, tied.effectiveSessionContext)
    }

    @Test fun yandexClientConfigurationHasNoDirectOrMailTransport() {
        val launch = CoreConfig.build(profile.copy(reserveRoute = ReserveRoute.Yandex), AppSettings(), paths)
        assertTrue("[Transport vyandex]" in launch.conf!!)
        assertTrue("[Transport yandex]" in launch.conf!!)
        assertFalse("[Transport direct]" in launch.conf!!)
        assertFalse("[Transport mailru]" in launch.conf!!)
        assertTrue("--session-context=https://docs.example/mail" in launch.arguments)
    }

    @Test fun exitStillServesEveryConfiguredCarrier() {
        val launch = CoreConfig.build(profile.copy(reserveRoute = ReserveRoute.Yandex), AppSettings(mode = ConnectionMode.Exit), paths)
        assertTrue("[Transport direct]" in launch.conf!!)
        assertTrue("[Transport mailru]" in launch.conf!!)
        assertEquals(ReserveRoute.Auto, profile.copy(reserveRoute = ReserveRoute.Yandex).forExit().reserveRoute)
    }

    @Test fun exportedForcedModeKeepsSaltAndOnlyYandexCarriers() {
        val share = profile.copy(reserveRoute = ReserveRoute.Yandex).toShare().getOrThrow()
        assertEquals("https://docs.example/mail", share.context)
        assertEquals(listOf("vyandex", "yandex"), share.transports.map { it.type })
        assertEquals(profile.secret, share.secret)
    }

    @Test fun routeSelectionPersistsAndOldProfilesDefaultToAutomatic() {
        val forced = profile.copy(reserveRoute = ReserveRoute.Yandex)
        assertEquals(forced, Json.decodeFromString<Profile>(Json.encodeToString(forced)))
        val legacy = Json.encodeToString(profile)
        assertFalse("reserveRoute" in legacy)
        assertEquals(ReserveRoute.Auto, Json.decodeFromString<Profile>(legacy).reserveRoute)
    }

    @Test fun unsupportedProfilesCannotSilentlyUseAnotherCarrier() {
        val direct = profile.copy(transport = TransportType.DIRECT, value = "203.0.113.10:33445", extras = emptyList(), reserveRoute = ReserveRoute.Yandex)
        assertFalse(direct.supportsReserveRoutes)
        assertTrue(direct.carriers.isEmpty())
        assertTrue(direct.toShare().isFailure)
        assertFailsWith<IllegalArgumentException> { CoreConfig.build(direct, AppSettings(), paths) }
    }

    @Test fun bridgeProfilesCarryTheSelectedNameWithoutJsonOrUriInjection() {
        for (route in ReserveRoute.entries) for (language in listOf("ru", "en")) {
            val title = route.label(language)
            val json = Json.parseToJsonElement(HiddifyBridge.profile(1080, title)).jsonObject
            assertEquals(title, json["outbounds"]!!.jsonArray.single().jsonObject["tag"]!!.jsonPrimitive.content)
            val link = HiddifyBridge.streisandLink("192.168.0.10", 1080, "a".repeat(64), title)
            assertEquals(title, URLDecoder.decode(URI(link).rawFragment, "UTF-8"))
        }
        val title = "quote\"\nnode"
        assertEquals(title, Json.parseToJsonElement(HiddifyBridge.profile(1080, title)).jsonObject["outbounds"]!!.jsonArray.single().jsonObject["tag"]!!.jsonPrimitive.content)
    }
}
