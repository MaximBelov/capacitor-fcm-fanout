package io.github.maximbelov.fcmfanout

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** A message in these tests: the name of the receiver that sent it, or null for one nobody sent. */
private data class Message(val sender: String?)

private class FakeReceiver(
    override val name: String,
    private val failOn: FanoutRouter.Stage? = null,
) : PushReceiver<Message> {
    val tokens = mutableListOf<String>()
    val handled = mutableListOf<Message>()
    var asked = 0

    override fun onNewToken(token: String) {
        if (failOn == FanoutRouter.Stage.TOKEN) throw IllegalStateException("$name token")
        tokens += token
    }

    override fun claims(message: Message): Boolean {
        asked++
        if (failOn == FanoutRouter.Stage.CLAIM) throw IllegalStateException("$name claim")
        return message.sender == name
    }

    override fun onMessage(message: Message) {
        if (failOn == FanoutRouter.Stage.MESSAGE) throw IllegalStateException("$name message")
        handled += message
    }
}

class FanoutRouterTest {
    private val errors = mutableListOf<Pair<String, FanoutRouter.Stage>>()

    private fun router(vararg receivers: FakeReceiver) =
        FanoutRouter(receivers.toList()) { receiver, stage, _ -> errors += receiver to stage }

    @Test
    fun `a token reaches every receiver`() {
        val intercom = FakeReceiver("intercom")
        val airship = FakeReceiver("airship")

        router(intercom, airship).onNewToken("t1")

        assertEquals(listOf("t1"), intercom.tokens)
        assertEquals(listOf("t1"), airship.tokens)
        assertEquals(emptyList<Pair<String, FanoutRouter.Stage>>(), errors)
    }

    @Test
    fun `a receiver that fails on a token does not cost the next one its token`() {
        val intercom = FakeReceiver("intercom", failOn = FanoutRouter.Stage.TOKEN)
        val airship = FakeReceiver("airship")

        router(intercom, airship).onNewToken("t1")

        assertEquals(listOf("t1"), airship.tokens)
        assertEquals(listOf("intercom" to FanoutRouter.Stage.TOKEN), errors)
    }

    @Test
    fun `a message goes to the receiver that claims it and to no other`() {
        val intercom = FakeReceiver("intercom")
        val airship = FakeReceiver("airship")
        val message = Message("airship")

        val by = router(intercom, airship).onMessage(message)

        assertEquals("airship", by)
        assertEquals(listOf(message), airship.handled)
        assertEquals(emptyList<Message>(), intercom.handled)
    }

    @Test
    fun `the first receiver to claim a message stops the search`() {
        val intercom = FakeReceiver("intercom")
        val airship = FakeReceiver("airship")

        val by = router(intercom, airship).onMessage(Message("intercom"))

        assertEquals("intercom", by)
        assertEquals(1, intercom.handled.size)
        assertEquals(0, airship.asked)
    }

    @Test
    fun `a message nobody claims is handled by nobody`() {
        val intercom = FakeReceiver("intercom")
        val airship = FakeReceiver("airship")

        assertNull(router(intercom, airship).onMessage(Message(null)))
        assertEquals(0, intercom.handled.size + airship.handled.size)
        assertEquals(1, intercom.asked)
        assertEquals(1, airship.asked)
    }

    @Test
    fun `a receiver whose check fails is passed over and the next one is asked`() {
        val intercom = FakeReceiver("intercom", failOn = FanoutRouter.Stage.CLAIM)
        val airship = FakeReceiver("airship")

        val by = router(intercom, airship).onMessage(Message("airship"))

        assertEquals("airship", by)
        assertEquals(1, airship.handled.size)
        assertEquals(listOf("intercom" to FanoutRouter.Stage.CLAIM), errors)
    }

    @Test
    fun `a receiver that fails to handle its message still claimed it, so no other gets it`() {
        val intercom = FakeReceiver("intercom", failOn = FanoutRouter.Stage.MESSAGE)
        val airship = FakeReceiver("airship")

        val by = router(intercom, airship).onMessage(Message("intercom"))

        assertEquals("intercom", by)
        assertEquals(0, airship.asked)
        assertEquals(listOf("intercom" to FanoutRouter.Stage.MESSAGE), errors)
    }

    @Test
    fun `a linkage error from an SDK at an unexpected version is reported, not thrown`() {
        val broken = object : PushReceiver<Message> {
            override val name = "airship"
            override fun onNewToken(token: String) = throw NoSuchMethodError("processNewToken")
            override fun claims(message: Message) = throw NoSuchMethodError("isAirshipPush")
            override fun onMessage(message: Message) = Unit
        }
        val router = FanoutRouter(listOf(broken)) { receiver, stage, _ -> errors += receiver to stage }

        router.onNewToken("t1")
        assertNull(router.onMessage(Message("airship")))

        assertEquals(listOf("airship" to FanoutRouter.Stage.TOKEN, "airship" to FanoutRouter.Stage.CLAIM), errors)
    }

    @Test
    fun `with no receivers nothing happens`() {
        val router = router()
        router.onNewToken("t1")
        assertNull(router.onMessage(Message("intercom")))
        assertEquals(emptyList<String>(), router.receiverNames)
    }

    @Test
    fun `the router reports its receivers in routing order`() {
        assertEquals(listOf("intercom", "airship"), router(FakeReceiver("intercom"), FakeReceiver("airship")).receiverNames)
    }
}
