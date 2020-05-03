package com.avp.wow.network.client

import com.avp.wow.network.NetworkConstants.DEFAULT_LOGIN_SERVER_HOST
import com.avp.wow.network.NetworkConstants.DEFAULT_LOGIN_SERVER_PORT
import io.ktor.network.selector.ActorSelectorManager
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import mu.KotlinLogging
import java.nio.ByteBuffer
import java.nio.ByteOrder

@KtorExperimentalAPI
class SimpleWowClient(
    private val host: String,
    private val port: Int
) {

    private val log = KotlinLogging.logger(this::class.java.name)

    val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun connect() {
        scope.launch {

            val socket = aSocket(ActorSelectorManager(scope.coroutineContext))
                .tcp()
                .connect(hostname = host, port = port)

            log.info { "Connected to ${socket.remoteAddress}" }

            val input = socket.openReadChannel()
            val output = socket.openWriteChannel(autoFlush = true)

            val rb = ByteBuffer.allocate(9000 * 2)
                .apply {
                    order(ByteOrder.BIG_ENDIAN)
                }

            scope.launch {

                while (true) {

                    val code = input.readAvailable(rb)

                    if (code != 0) {
                        log.debug { "Received packet with len: ${rb.remaining()}" }
                        //println("server: $line")

                        rb.clear()
                    }

                }

            }

        }
    }

}

@KtorExperimentalAPI
inline fun buildWowClient(
    host: String = DEFAULT_LOGIN_SERVER_HOST,
    port: Int = DEFAULT_LOGIN_SERVER_PORT,
    configure: SimpleWowClient.() -> Unit
) = SimpleWowClient(host = host, port = port)
    .apply {
        configure()
        connect()
    }