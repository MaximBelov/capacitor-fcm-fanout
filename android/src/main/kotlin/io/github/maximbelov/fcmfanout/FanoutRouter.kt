package io.github.maximbelov.fcmfanout

/**
 * Hands FCM tokens and messages to the [receivers]:
 *
 * - a token goes to every receiver, since each registers it with its own backend;
 * - a message goes to the first receiver, in order, that claims it, and to no other: a message belongs to the service
 *   that sent it, and a second SDK handling it would show a second notification.
 *
 * A receiver that throws is reported to [onError] and does not stop the others: an SDK failing to register a token
 * must not cost the next one its token.
 */
class FanoutRouter<M>(
    private val receivers: List<PushReceiver<M>>,
    private val onError: (receiver: String, stage: Stage, error: Throwable) -> Unit,
) {
    /** Where a receiver failed, as [onError] reports it. */
    enum class Stage { TOKEN, CLAIM, MESSAGE }

    /** The receivers' names, in the order a message is offered to them. */
    val receiverNames: List<String> get() = receivers.map { it.name }

    fun onNewToken(token: String) {
        for (receiver in receivers) {
            guard(receiver, Stage.TOKEN) { receiver.onNewToken(token) }
        }
    }

    /**
     * Offers [message] to the receivers in order and returns the name of the one that claimed it, or null when none did.
     * A receiver whose check throws is treated as not claiming the message, so the next one is still asked.
     */
    fun onMessage(message: M): String? {
        for (receiver in receivers) {
            val claimed = guard(receiver, Stage.CLAIM) { receiver.claims(message) } ?: false
            if (claimed) {
                guard(receiver, Stage.MESSAGE) { receiver.onMessage(message) }
                return receiver.name
            }
        }
        return null
    }

    private fun <T> guard(receiver: PushReceiver<M>, stage: Stage, block: () -> T): T? =
        try {
            block()
        } catch (error: Exception) {
            onError(receiver.name, stage, error)
            null
        } catch (error: LinkageError) {
            // An SDK present at a version whose API differs from the one this plugin was compiled against.
            onError(receiver.name, stage, error)
            null
        }
}
