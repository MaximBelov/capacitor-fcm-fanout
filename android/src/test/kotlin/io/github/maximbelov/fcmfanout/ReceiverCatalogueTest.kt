package io.github.maximbelov.fcmfanout

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReceiverCatalogueTest {
    @Test
    fun `both SDKs present gives both receivers, Intercom first`() {
        assertEquals(listOf("intercom", "airship"), ReceiverCatalogue.available { true })
    }

    @Test
    fun `a missing SDK leaves its receiver out`() {
        val onlyAirship = ReceiverCatalogue.available { it == ReceiverCatalogue.requiredClasses.getValue("airship") }
        val onlyIntercom = ReceiverCatalogue.available { it == ReceiverCatalogue.requiredClasses.getValue("intercom") }

        assertEquals(listOf("airship"), onlyAirship)
        assertEquals(listOf("intercom"), onlyIntercom)
        assertEquals(emptyList<String>(), ReceiverCatalogue.available { false })
    }

    @Test
    fun `the class check finds a class that is there`() {
        assertTrue(ReceiverCatalogue.classPresent("java.lang.String"))
    }

    @Test
    fun `the class check reports a missing class as absent instead of throwing`() {
        assertFalse(ReceiverCatalogue.classPresent("com.example.NoSuchClass"))
    }

    // The SDKs are compileOnly, so they are absent from the test runtime exactly as from an app without them: this is
    // the default check meeting a real missing class, not a fake.
    @Test
    fun `the compile-only SDKs are not on the runtime classpath, so the default check leaves both out`() {
        assertEquals(emptyList<String>(), ReceiverCatalogue.available())
    }
}
