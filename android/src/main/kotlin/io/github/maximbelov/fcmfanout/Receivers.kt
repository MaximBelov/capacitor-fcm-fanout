package io.github.maximbelov.fcmfanout

import android.app.Application
import android.content.Context
import com.google.firebase.messaging.RemoteMessage
import com.urbanairship.push.fcm.AirshipFirebaseIntegration
import io.intercom.android.sdk.push.IntercomPushClient

/**
 * The receivers backed by the real SDKs. Each class touches its SDK only from its own methods, so it is loaded only
 * when [create] asks for it, which it does only for an SDK [ReceiverCatalogue] found.
 */
internal object Receivers {
    fun create(application: Application, names: List<String> = ReceiverCatalogue.available()): List<PushReceiver<RemoteMessage>> =
        names.map { name ->
            when (name) {
                ReceiverCatalogue.INTERCOM -> IntercomReceiver(application)
                ReceiverCatalogue.AIRSHIP -> AirshipReceiver(application)
                else -> error("unknown receiver $name")
            }
        }
}

/** Intercom's conversation pushes, through the client its documentation gives an app with its own FCM service. */
internal class IntercomReceiver(private val application: Application) : PushReceiver<RemoteMessage> {
    private val client = IntercomPushClient()

    override val name = ReceiverCatalogue.INTERCOM

    override fun onNewToken(token: String) {
        client.sendTokenToIntercom(application, token)
    }

    override fun claims(message: RemoteMessage): Boolean = client.isIntercomPush(message.data)

    override fun onMessage(message: RemoteMessage) {
        client.handlePush(application, message.data)
    }
}

/** Airship, through the integration its documentation gives an app with its own FCM service. */
internal class AirshipReceiver(private val context: Context) : PushReceiver<RemoteMessage> {
    override val name = ReceiverCatalogue.AIRSHIP

    override fun onNewToken(token: String) {
        AirshipFirebaseIntegration.processNewToken(context, token)
    }

    override fun claims(message: RemoteMessage): Boolean = AirshipFirebaseIntegration.isAirshipPush(message)

    override fun onMessage(message: RemoteMessage) {
        // Sync: the service's onMessageReceived is the only time the process is guaranteed to be kept alive for it.
        AirshipFirebaseIntegration.processMessageSync(context, message)
    }
}
