package com.avp.wow.network

import com.avp.wow.network.helpers.SimpleTcpClient
import com.avp.wow.network.todo_move.LoginConnectionFactory
import io.kotlintest.specs.StringSpec
import io.ktor.network.selector.ActorSelectorManager
import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.util.KtorExperimentalAPI
import io.ktor.utils.io.readUTF8Line
import io.ktor.utils.io.writeStringUtf8
import kotlinx.coroutines.*
import java.net.InetSocketAddress
import java.util.concurrent.Executors
import kotlin.concurrent.thread

@KtorExperimentalAPI
class NioServerTests : StringSpec({

    "!:cancellation test 3" {

        class T {
            val scope = CoroutineScope(Job() + Dispatchers.IO)
            val ctx = scope.coroutineContext

            fun connect() {
                scope.launch {
                    while (true) {
                        delay(100)
                    }
                }
            }

            fun shutdown() = runBlocking(ctx) {
                scope.cancel("123")
            }

        }

        val t = T()

        t.connect()

        t.shutdown()

    }

    "!:cancellation test 2" {

        val scope = CoroutineScope (
            Job() + Dispatchers.IO
        )

        val server = aSocket(ActorSelectorManager(scope.coroutineContext)).tcp().bind(InetSocketAddress(2424))

        val job = scope.launch {

            try {
                while (true) {

                    val socket = server.accept()

                    try {
                        while (true) {
                            //delay(100)

                            socket.openReadChannel()
                                .readUTF8Line()

                        }
                    } catch (e: CancellationException){
                        println("Work cancelled!")
                    } finally {
                        println("Clean up!")
                    }

                }
            } catch (e: CancellationException){
                println("Work cancelled!")
            } finally {
                println("Clean up!")
            }

        }
        delay(1000L)
        println("Cancel!")
        scope.cancel()
        println("Done!")

    }

    "!:cancellation test" {

        val scope = CoroutineScope (
            Job() + Dispatchers.IO
        )

        val job = scope.launch {
            try {
                while (true) {
                    delay(100)
                }
            } catch (e: CancellationException){
                println("Work cancelled!")
            } finally {
                println("Clean up!")
            }
        }
        delay(1000L)
        println("Cancel!")
        scope.cancel()
        println("Done!")

    }

    "f:test" {

        val host = "127.0.0.1"
        val port = 2323

        val loginServerConfig = ServerConfig(
            hostName = host,
            port = port,
            connectionName = "Test Login Connection",
            factory = LoginConnectionFactory()
        )

        //val client = SimpleTcpClient(host, port, true, Dispatchers.IO)

        val server = NioServer(
            serverConfigs = listOf(
                loginServerConfig
            ),
            context = Dispatchers.IO
        )

        server.connect()

        //delay(1000)

        /*client.send("test")
        delay(100)
        client.send("test 2")*/

        val client = aSocket(ActorSelectorManager(Dispatchers.IO))
            .tcp()
            .connect(InetSocketAddress("127.0.0.1", 2323))
        val output = client.openWriteChannel(autoFlush = true)
        output.writeStringUtf8("hello\r\n")
        output.writeStringUtf8("world\r\n")
        output.writeStringUtf8("exit\r\n")

        delay(2000)

        server.shutdown()

    }

}) {



}