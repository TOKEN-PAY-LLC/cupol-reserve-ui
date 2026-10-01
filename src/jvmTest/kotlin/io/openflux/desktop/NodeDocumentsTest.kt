package io.openflux.desktop

import io.openflux.desktop.model.NodeDocuments
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class NodeDocumentsTest {
    private val path = "/edit/d/AbCdEfGhIjKlMnOpQrStUv"

    @Test
    fun accepts360EditorAndRemovesTracking() {
        for (host in listOf("docs.360.yandex.com", "disk.360.yandex.com")) {
            val url = "https://$host$path"
            assertEquals(url, NodeDocuments.clean(" $url/?source=docs#section "))
        }
    }

    @Test
    fun preservesSupportedLegacyHosts() {
        for (host in listOf("docs.yandex.ru", "disk.yandex.com", "docs.yandex.kz")) {
            assertEquals("https://$host$path", NodeDocuments.clean("https://$host$path"))
        }
    }

    @Test
    fun rejectsLookalikeHostsAndDownloadLinks() {
        for (host in listOf("docs.360.yandex.com.evil.example", "docs.360.yandex.ru", "evil.yandex.com", "docs.yandex.com@evil.example")) {
            assertNull(NodeDocuments.clean("https://$host$path"))
        }
        assertNull(NodeDocuments.clean("https://disk.360.yandex.com/i/AbCdEfGhIjKlMnOpQrStUv"))
    }
}
