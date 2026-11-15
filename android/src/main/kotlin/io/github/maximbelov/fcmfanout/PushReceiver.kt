package io.github.maximbelov.fcmfanout

/**
 * An SDK that takes FCM tokens and messages. [M] is the message: Firebase's `RemoteMessage` in an app, a plain value in
 * the JVM tests, which have no Android runtime.
 */
interface PushReceiver<M> {
    /** A short name, as `getStatus` reports it and the log lines print it. */
    val name: String

    /** Called with every new FCM token. */
    fun onNewToken(token: String)

    /** Whether [message] was sent through this receiver's own service, so this receiver is the one to handle it. */
    fun claims(message: M): Boolean

    /** Called with a message this receiver claimed. */
    fun onMessage(message: M)
}
