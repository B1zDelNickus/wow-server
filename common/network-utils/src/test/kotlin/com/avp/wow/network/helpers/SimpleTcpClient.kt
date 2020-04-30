package com.avp.wow.network.helpers

import io.ktor.network.selector.ActorSelectorManager
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.isClosed
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.util.cio.write
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.*
import mu.KotlinLogging
import java.net.InetSocketAddress
import kotlin.coroutines.CoroutineContext

class SimpleTcpClient(
    private val host: String,
    private val port: Int,
    private val autoFlush: Boolean = true,
    override val coroutineContext: CoroutineContext = Dispatchers.IO
) : CoroutineScope {

    private val log = KotlinLogging.logger(this::class.java.name)

    private val client by lazy {
        runBlocking(coroutineContext) { aSocket(ActorSelectorManager(coroutineContext)).tcp().connect(InetSocketAddress(host, port))  }
    }

    private val input by lazy { client.openReadChannel() }
    private val output by lazy { client.openWriteChannel(autoFlush = autoFlush) }

    val isActive get() = !client.isClosed

    init {
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
    }

    suspend fun send(msg: Any) {
        when (msg) {
            is String -> output.write("$msg\r\n")
            else -> {

            }
        }
    }

    fun shutdown() {
        try {
            client.close()
        } catch (ignored: Exception) {}
    }

}