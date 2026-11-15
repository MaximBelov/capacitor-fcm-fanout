package io.github.maximbelov.fcmfanout

/**
 * The receivers the plugin knows, in the order a message is offered to them, each with the SDK class it needs.
 *
 * The plugin compiles against both SDKs and ships neither, so a receiver is used only when the app has its SDK. The
 * check reads the class without initialising it.
 */
object ReceiverCatalogue {
    const val INTERCOM = "intercom"
    const val AIRSHIP = "airship"

    /**
     * Each SDK's check looks for keys its own backend puts in the data, so in practice at most one claims a message. The
     * order is fixed anyway, so that a message carrying both is never handled twice and always by the same one.
     */
    val requiredClasses: Map<String, String> = linkedMapOf(
        INTERCOM to "io.intercom.android.sdk.push.IntercomPushClient",
        AIRSHIP to "com.urbanairship.push.fcm.AirshipFirebaseIntegration",
    )

    /** The names of the receivers whose SDK class [isPresent] finds, in routing order. */
    fun available(isPresent: (String) -> Boolean = ::classPresent): List<String> =
        requiredClasses.filterValues(isPresent).keys.toList()

    fun classPresent(className: String): Boolean =
        try {
            Class.forName(className, false, ReceiverCatalogue::class.java.classLoader)
            true
        } catch (_: ClassNotFoundException) {
            false
        } catch (_: LinkageError) {
            false
        }
}
