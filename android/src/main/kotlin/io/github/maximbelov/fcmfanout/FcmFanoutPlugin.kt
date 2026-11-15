package io.github.maximbelov.fcmfanout

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import com.getcapacitor.JSArray
import com.getcapacitor.JSObject
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.CapacitorPlugin

/**
 * The JavaScript side of the plugin, which only reports. The routing is [FcmFanoutMessagingService], which Firebase
 * starts on its own whether or not the web view is up.
 */
@CapacitorPlugin(name = "FcmFanout")
class FcmFanoutPlugin : Plugin() {
    @PluginMethod
    fun getStatus(call: PluginCall) {
        val services = messagingServices(context)
        val result = JSObject()
        result.put("messagingServices", JSArray(services))
        result.put("active", FanoutStatus.isActive(services))
        result.put("receivers", JSArray(ReceiverCatalogue.available()))
        call.resolve(result)
    }

    /** The services of this app that resolve Firebase's messaging action, as the merged manifest declares them. */
    private fun messagingServices(context: Context): List<String> {
        val intent = Intent(FanoutStatus.MESSAGING_EVENT).setPackage(context.packageName)
        val packageManager = context.packageManager
        val resolved =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.queryIntentServices(intent, PackageManager.ResolveInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                packageManager.queryIntentServices(intent, 0)
            }
        return resolved.mapNotNull { it.serviceInfo?.name }
    }
}

/** What `getStatus` concludes from the services it found. Plain Kotlin, so the JVM tests reach it. */
object FanoutStatus {
    const val MESSAGING_EVENT = "com.google.firebase.MESSAGING_EVENT"

    /** [FcmFanoutMessagingService]'s name, as the manifest declares it. A test holds the two together. */
    const val SERVICE = "io.github.maximbelov.fcmfanout.FcmFanoutMessagingService"

    /**
     * Firebase's own base class, which the firebase-messaging library declares on the action at priority -500 so that
     * an app without a service of its own still gets notification messages. Any other service outranks it.
     */
    const val FIREBASE_FALLBACK = "com.google.firebase.messaging.FirebaseMessagingService"

    /**
     * The plugin's service is the only one besides Firebase's fallback: Firebase delivers to one service per action, so
     * with another one beside it, it may pick the other.
     */
    fun isActive(services: List<String>): Boolean = services.filter { it != FIREBASE_FALLBACK } == listOf(SERVICE)
}
