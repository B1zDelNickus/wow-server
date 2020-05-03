package com.avp.wow.network.helpers

import io.ktor.network.selector.ActorSelectorManager
import io.ktor.network.sockets.*
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.cio.write
import kotlinx.coroutines.*
import mu.KotlinLogging
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.coroutines.CoroutineContext

@KtorExperimentalAPI
class SimpleTcpClient(
    private val host: String,
    private val port: Int,
    private val autoFlush: Boolean = true,
    context: CoroutineContext = Dispatchers.IO
) {

    private val scope = CoroutineScope(SupervisorJob() + context)

    val log = KotlinLogging.logger(this::class.java.name)

    private lateinit var client: Socket

    val input by lazy { client.openReadChannel() }
    val output by lazy { client.openWriteChannel(autoFlush = autoFlush) }

    val isActive get() = !client.isClosed

    private var initiated = false

    /*init {
        launch(coroutineContext) {
            while (isActive) {
                try {
                    input.readUTF8Line()?.let { line ->
                        when (line.toLowerCase()) {
                            "exit" -> shutdown()
                            else -> log.info { "Message received: $line" }
                        }
                    }
                } catch (ignored: Exception) {}
            }
        }
    }*/

    suspend fun connect() {
        client = aSocket(ActorSelectorManager(scope.coroutineContext)).tcp().connect(InetSocketAddress(host, port))
        initiated = true
    }

    suspend fun send(msg: Any) {
        if (!initiated) throw IllegalStateException("Client not connected to server")
        when (msg) {
            is String -> scope.launch { output.write("$msg\r\n") }
            else -> {

            }
        }
    }

    suspend fun <T> sendPkt(msg: T) {
        if (!initiated) throw IllegalStateException("Client not connected to server")
        when (msg) {
            /*is CM_TEST_FAST -> scope.launch {
                val buf = ByteBuffer.allocate(8096 * 2)
                buf.order(ByteOrder.BIG_ENDIAN)
                buf.putInt(0x99)
                output.writeAvailable(buf)
                log.info { "CM_TEST_FAST were sent to server" }
            }*/
            else -> {

            }
        }
    }

    suspend fun <T> read(clz: T) : T {
        if (!initiated) throw IllegalStateException("Client not connected to server")
        return when (clz) {
            String -> {
                //output.write("$msg\r\n")
                scope.async { "" as T }
            }
            else -> {
                scope.async { "" as T }
            }
        }.await()
    }

    suspend fun executeSequence(block: suspend SimpleTcpClient.() -> Unit) =
        scope.launch { block.invoke(this@SimpleTcpClient) }

    fun shutdown() {
        initiated = false
        try {
            client.close()
            client.dispose()
        } catch (ignored: Exception) {}
        scope.cancel()
    }

}