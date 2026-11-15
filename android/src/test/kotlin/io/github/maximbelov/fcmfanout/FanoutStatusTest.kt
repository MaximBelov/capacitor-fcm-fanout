package io.github.maximbelov.fcmfanout

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FanoutStatusTest {
    @Test
    fun `the service name is the class the manifest declares`() {
        assertEquals(FcmFanoutMessagingService::class.java.name, FanoutStatus.SERVICE)
        val manifest = File("src/main/AndroidManifest.xml").readText()
        assertTrue(manifest.contains("android:name=\"${FanoutStatus.SERVICE}\""))
        assertTrue(manifest.contains("<action android:name=\"${FanoutStatus.MESSAGING_EVENT}\" />"))
    }

    @Test
    fun `the plugin's service alone is active`() {
        assertTrue(FanoutStatus.isActive(listOf(FanoutStatus.SERVICE)))
    }

    @Test
    fun `Firebase's own low-priority fallback beside it does not matter`() {
        assertTrue(FanoutStatus.isActive(listOf(FanoutStatus.SERVICE, FanoutStatus.FIREBASE_FALLBACK)))
        assertFalse(FanoutStatus.isActive(listOf(FanoutStatus.FIREBASE_FALLBACK)))
    }

    @Test
    fun `another service beside it is not active, since Firebase may pick the other one`() {
        assertFalse(
            FanoutStatus.isActive(
                listOf(FanoutStatus.SERVICE, "com.urbanairship.push.fcm.AirshipFirebaseMessagingService"),
            ),
        )
    }

    @Test
    fun `no service, or only another one, is not active`() {
        assertFalse(FanoutStatus.isActive(emptyList()))
        assertFalse(FanoutStatus.isActive(listOf("io.capawesome.capacitorjs.plugins.firebase.messaging.MessagingService")))
    }
}
