package de.sksdesign.kramerswitch.core

/**
 * Kramer Protocol 2000 helper for the Kramer VS-81H 8x1 HDMI switcher.
 *
 * Packet format used here:
 * byte 1: instruction, 0x01 for SWITCH VIDEO, 0x05 for REQUEST STATUS OF VIDEO OUTPUT
 * byte 2: 0x80 + input/setup value
 * byte 3: 0x80 + output/value
 * byte 4: 0x80 + machine number
 */
object KramerProtocol {
    const val DEFAULT_IP = "192.168.1.39"
    const val DEFAULT_PORT = 5000
    const val DEFAULT_TIMEOUT_MS = 1200
    const val DEFAULT_MACHINE_NUMBER = 1
    const val VS81H_OUTPUT = 1

    private const val INSTRUCTION_SWITCH_VIDEO = 0x01
    private const val INSTRUCTION_REQUEST_VIDEO_OUTPUT_STATUS = 0x05
    private const val DESTINATION_REPLY_BIT = 0x40
    private const val DATA_BYTE_MARKER = 0x80

    data class Reply(
        val raw: ByteArray,
        val instruction: Int,
        val input: Int,
        val output: Int,
        val machineNumber: Int,
        val isReplyFromDevice: Boolean,
        val hex: String
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Reply) return false
            return raw.contentEquals(other.raw) &&
                instruction == other.instruction &&
                input == other.input &&
                output == other.output &&
                machineNumber == other.machineNumber &&
                isReplyFromDevice == other.isReplyFromDevice &&
                hex == other.hex
        }

        override fun hashCode(): Int {
            var result = raw.contentHashCode()
            result = 31 * result + instruction
            result = 31 * result + input
            result = 31 * result + output
            result = 31 * result + machineNumber
            result = 31 * result + isReplyFromDevice.hashCode()
            result = 31 * result + hex.hashCode()
            return result
        }
    }

    fun buildSwitchInputCommand(
        input: Int,
        output: Int = VS81H_OUTPUT,
        machineNumber: Int = DEFAULT_MACHINE_NUMBER
    ): ByteArray {
        require(input in 0..8) { "VS-81H input must be 0..8, where 0 disconnects the output." }
        require(output in 1..1) { "VS-81H has exactly one HDMI output. Use output 1." }
        require(machineNumber in 1..31) { "Kramer Protocol 2000 machine number must be 1..31." }

        return byteArrayOf(
            INSTRUCTION_SWITCH_VIDEO.toByte(),
            encodeDataByte(input),
            encodeDataByte(output),
            encodeDataByte(machineNumber)
        )
    }

    fun buildRequestActiveInputCommand(
        output: Int = VS81H_OUTPUT,
        machineNumber: Int = DEFAULT_MACHINE_NUMBER
    ): ByteArray {
        require(output in 1..1) { "VS-81H has exactly one HDMI output. Use output 1." }
        require(machineNumber in 1..31) { "Kramer Protocol 2000 machine number must be 1..31." }

        return byteArrayOf(
            INSTRUCTION_REQUEST_VIDEO_OUTPUT_STATUS.toByte(),
            encodeDataByte(0), // setup #0 = current status
            encodeDataByte(output),
            encodeDataByte(machineNumber)
        )
    }

    fun parseReply(bytes: ByteArray): Reply? {
        if (bytes.size < 4) return null
        val first = bytes[0].toInt() and 0xFF
        return Reply(
            raw = bytes.take(4).toByteArray(),
            instruction = first and 0x3F,
            input = bytes[1].toInt() and 0x7F,
            output = bytes[2].toInt() and 0x7F,
            machineNumber = bytes[3].toInt() and 0x1F,
            isReplyFromDevice = (first and DESTINATION_REPLY_BIT) == DESTINATION_REPLY_BIT,
            hex = toHex(bytes.take(4).toByteArray())
        )
    }

    fun isSwitchAck(reply: Reply?, input: Int, machineNumber: Int = DEFAULT_MACHINE_NUMBER): Boolean {
        if (reply == null) return false
        return reply.isReplyFromDevice &&
            reply.instruction == INSTRUCTION_SWITCH_VIDEO &&
            reply.input == input &&
            reply.output == VS81H_OUTPUT &&
            reply.machineNumber == machineNumber
    }

    /**
     * Parses active input from either:
     * - SWITCH VIDEO reply/event: 0x41, 0x8[input], 0x81, 0x8[machine]
     * - REQUEST STATUS reply:     0x45, 0x80, 0x8[input], 0x8[machine]
     */
    fun activeInputFromReply(reply: Reply?): Int? {
        if (reply == null || !reply.isReplyFromDevice) return null
        return when (reply.instruction) {
            INSTRUCTION_SWITCH_VIDEO -> reply.input.takeIf { it in 0..8 }
            INSTRUCTION_REQUEST_VIDEO_OUTPUT_STATUS -> reply.output.takeIf { it in 0..8 }
            else -> null
        }
    }

    fun toHex(bytes: ByteArray): String = bytes.joinToString(" ") { byte ->
        (byte.toInt() and 0xFF).toString(16).uppercase().padStart(2, '0')
    }

    private fun encodeDataByte(value: Int): Byte = (DATA_BYTE_MARKER or (value and 0x7F)).toByte()
}
