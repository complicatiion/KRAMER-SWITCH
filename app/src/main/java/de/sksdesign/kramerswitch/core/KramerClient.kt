package de.sksdesign.kramerswitch.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketTimeoutException

class KramerClient(
    private val host: String,
    private val port: Int,
    private val timeoutMs: Int,
    private val machineNumber: Int,
    private val requireReply: Boolean
) {
    sealed class ClientResult {
        data class Success(
            val activeInput: Int?,
            val replyHex: String?,
            val message: String
        ) : ClientResult()

        data class Failure(
            val message: String,
            val replyHex: String? = null
        ) : ClientResult()
    }

    suspend fun testConnection(): ClientResult = withContext(Dispatchers.IO) {
        runCatching {
            openSocket().use { socket ->
                socket.isConnected
            }
        }.fold(
            onSuccess = { ClientResult.Success(null, null, "Connection successful") },
            onFailure = { ClientResult.Failure(toUserMessage(it)) }
        )
    }

    suspend fun switchInput(input: Int): ClientResult = withContext(Dispatchers.IO) {
        runCatching {
            val command = KramerProtocol.buildSwitchInputCommand(input, machineNumber = machineNumber)
            sendCommand(command)
        }.fold(
            onSuccess = { reply ->
                if (!requireReply) {
                    ClientResult.Success(input.takeIf { it != 0 }, reply?.hex, "Command sent")
                } else if (KramerProtocol.isSwitchAck(reply, input, machineNumber)) {
                    ClientResult.Success(
                        KramerProtocol.activeInputFromReply(reply)?.takeIf { it != 0 },
                        reply?.hex,
                        if (input == 0) "Output disconnected" else "HDMI $input active"
                    )
                } else {
                    ClientResult.Failure(
                        message = "Unexpected or missing reply from the switch",
                        replyHex = reply?.hex
                    )
                }
            },
            onFailure = { ClientResult.Failure(toUserMessage(it)) }
        )
    }

    suspend fun refreshActiveInput(): ClientResult = withContext(Dispatchers.IO) {
        runCatching {
            val command = KramerProtocol.buildRequestActiveInputCommand(machineNumber = machineNumber)
            sendCommand(command, alwaysReadReply = true)
        }.fold(
            onSuccess = { reply ->
                val active = KramerProtocol.activeInputFromReply(reply)
                if (active != null) {
                    ClientResult.Success(
                        active.takeIf { it != 0 },
                        reply?.hex,
                        if (active == 0) "Output disconnected" else "Active input: HDMI $active"
                    )
                } else {
                    ClientResult.Failure(
                        message = "Status could not be read clearly",
                        replyHex = reply?.hex
                    )
                }
            },
            onFailure = { ClientResult.Failure(toUserMessage(it)) }
        )
    }

    private fun sendCommand(command: ByteArray, alwaysReadReply: Boolean = false): KramerProtocol.Reply? {
        openSocket().use { socket ->
            val output = socket.getOutputStream()
            output.write(command)
            output.flush()

            if (!requireReply && !alwaysReadReply) return null

            val response = ByteArray(4)
            var offset = 0
            while (offset < 4) {
                val read = socket.getInputStream().read(response, offset, response.size - offset)
                if (read < 0) break
                offset += read
            }
            return KramerProtocol.parseReply(response.copyOf(offset))
        }
    }

    private fun openSocket(): Socket {
        val socket = Socket()
        socket.soTimeout = timeoutMs
        socket.tcpNoDelay = true
        socket.connect(InetSocketAddress(host, port), timeoutMs)
        return socket
    }

    private fun toUserMessage(error: Throwable): String = when (error) {
        is SocketTimeoutException -> "Timeout: no response from $host:$port"
        is java.net.ConnectException -> "Connection refused or device unreachable: $host:$port"
        is java.net.UnknownHostException -> "Invalid IP address or hostname: $host"
        is IllegalArgumentException -> error.message ?: "Invalid setting"
        else -> error.message ?: "Unknown network error"
    }
}
