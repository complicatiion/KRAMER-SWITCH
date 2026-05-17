package de.sksdesign.kramerswitch.core

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KramerProtocolTest {
    @Test
    fun switchInputOneBuildsExpectedVs81hPacket() {
        assertArrayEquals(
            byteArrayOf(0x01, 0x81.toByte(), 0x81.toByte(), 0x81.toByte()),
            KramerProtocol.buildSwitchInputCommand(1)
        )
    }

    @Test
    fun switchInputEightBuildsExpectedVs81hPacket() {
        assertArrayEquals(
            byteArrayOf(0x01, 0x88.toByte(), 0x81.toByte(), 0x81.toByte()),
            KramerProtocol.buildSwitchInputCommand(8)
        )
    }

    @Test
    fun disconnectOutputBuildsExpectedVs81hPacket() {
        assertArrayEquals(
            byteArrayOf(0x01, 0x80.toByte(), 0x81.toByte(), 0x81.toByte()),
            KramerProtocol.buildSwitchInputCommand(0)
        )
    }

    @Test
    fun requestActiveInputBuildsExpectedPacket() {
        assertArrayEquals(
            byteArrayOf(0x05, 0x80.toByte(), 0x81.toByte(), 0x81.toByte()),
            KramerProtocol.buildRequestActiveInputCommand()
        )
    }

    @Test
    fun switchAckParsesActiveInput() {
        val reply = KramerProtocol.parseReply(byteArrayOf(0x41, 0x83.toByte(), 0x81.toByte(), 0x81.toByte()))
        assertTrue(KramerProtocol.isSwitchAck(reply, input = 3))
        assertEquals(3, KramerProtocol.activeInputFromReply(reply))
    }

    @Test
    fun statusReplyParsesActiveInput() {
        val reply = KramerProtocol.parseReply(byteArrayOf(0x45, 0x80.toByte(), 0x84.toByte(), 0x81.toByte()))
        assertEquals(4, KramerProtocol.activeInputFromReply(reply))
    }

    @Test
    fun nonDestinationPacketIsNotAcceptedAsAck() {
        val reply = KramerProtocol.parseReply(byteArrayOf(0x01, 0x83.toByte(), 0x81.toByte(), 0x81.toByte()))
        assertFalse(KramerProtocol.isSwitchAck(reply, input = 3))
    }

    @Test
    fun hexFormattingIsUppercaseAndPadded() {
        assertEquals("01 88 81 81", KramerProtocol.toHex(KramerProtocol.buildSwitchInputCommand(8)))
    }
}
