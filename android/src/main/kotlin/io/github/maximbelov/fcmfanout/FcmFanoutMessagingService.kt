package io.github.maximbelov.fcmfanout

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * The app's one FCM service: every new token goes to each SDK in the app, and every message to the SDK that sent it.
 * A message no SDK claims is left alone; a notification message is still shown by Firebase itself when the app is in
 * the background, as it is with any service.
 */
class FcmFanoutMessagingService : FirebaseMessagingService() {
    private val router by lazy {
        FanoutRouter(Receivers.create(application)) { receiver, stage, error ->
            Log.e(TAG, "$receiver failed at ${stage.name.lowercase()}", error)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        router.onNewToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        if (router.onMessage(message) == null) {
            Log.d(TAG, "no receiver claims message ${message.messageId}; receivers: ${router.receiverNames}")
        }
    }

    companion object {
        private const val TAG = "FcmFanout"
    }
}
