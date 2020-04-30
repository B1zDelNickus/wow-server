package experiments

import io.kotlintest.specs.StringSpec
import io.ktor.network.selector.ActorSelectorManager
import io.ktor.network.sockets.*
import io.ktor.utils.io.readUTF8Line
import io.ktor.utils.io.writeStringUtf8
import kotlinx.coroutines.*
import mu.KotlinLogging
import java.net.InetSocketAddress
import java.util.concurrent.CopyOnWriteArrayList

class Ex2 : StringSpec({

    "test" {

        val server = TestServer()

        server.connect()

        val client = aSocket(ActorSelectorManager(Dispatchers.IO))
            .tcp()
            .connect(InetSocketAddress("127.0.0.1", 2323))
        val output = client.openWriteChannel(autoFlush = true)
        output.writeStringUtf8("hello\r\n")
        output.writeStringUtf8("world\r\n")
        output.writeStringUtf8("exit\r\n")

        delay(1000)

        server.shutdown()

        delay(1000)

    }

})

class TestServer {

    private val log = KotlinLogging.logger(this::class.java.name)

    private val connections = CopyOnWriteArrayList<TestConnection>()

    fun connect() {
        GlobalScope.launch(Dispatchers.IO) {

            log.info { "Start connection..." }

            val server = aSocket(ActorSelectorManager(Dispatchers.IO))
                .tcp()
                .bind(InetSocketAddress("127.0.0.1", 2323))

            launch {

                TestLoginConnection(server, this@TestServer).apply { accept() }

                /*val socket = server.accept()
                //connections.add(socket)

                log.info { "Accepting connection from ${socket.remoteAddress}" }

                launch {

                    val input = socket.openReadChannel()
                    val output = socket.openWriteChannel(autoFlush = true)

                    var isActive = true

                    while (isActive) {

                        try {
                            if (socket.isClosed) {
                                isActive = false
                                break
                            }
                            input.readUTF8Line()?.also { line ->
                                when (line.toLowerCase()) {
                                    "exit" -> {
                                        log.info { "Exit msg received" }
                                        socket.close()
                                        socket.dispose()
                                        //connections.remove(socket)
                                        isActive = false
                                    }
                                    else -> log.info { "Msg received: $line" }
                                }
                            }
                        } catch (e: Exception) {
                            socket.close()
                            socket.dispose()
                            //connections.remove(socket)
                        }
                    }

                    log.info { "Closing connection job" }

                }*/

            }

        }
    }

    fun shutdown() = runBlocking(Dispatchers.IO) {

        log.info { "Closing all connections and shutdown" }

        connections.forEach { conn ->
            conn.close()
        }
        connections.clear()

        delay(100)

        Dispatchers.IO.cancelChildren()
        Dispatchers.IO.cancel()

        log.info { "Active connections: ${connections.size}" }

    }

    fun addConnection(connection: TestConnection) {
        connections.add(connection)
        log.info { "Accepted connection from ${connection.socket?.remoteAddress}" }
    }

    fun closeConnection(connection: TestConnection) {
        val host = connection.socket?.remoteAddress ?: "unknown"
        connection.pendingClose = true
        connection.socket?.close()
        connection.socket?.dispose()
        connections.remove(connection)
        log.info { "Connection from $host connection status: ${connection.socket?.isClosed == false}" }
    }

}

abstract class TestConnection(
    val serverSocket: ServerSocket,
    val nioServer: TestServer
) {

    protected val log = KotlinLogging.logger(this::class.java.name)

    var socket: Socket? = null

    var pendingClose = false

    val isActive get() = !(socket?.isClosed ?: true) && !pendingClose

    val input by lazy {
        socket?.let { sk -> if (isActive) sk.openReadChannel() else throw RuntimeException("Read channel is closed ot null") }
    }

    val output by lazy {
        socket?.let { sk -> if (isActive) sk.openWriteChannel(autoFlush = true) else throw RuntimeException("Write channel is closed ot null") }
    }

    val guard = Any()

    abstract suspend fun dispatch()

    suspend fun accept() {
        try {
            socket = serverSocket.accept()
            nioServer.addConnection(this)
            GlobalScope.launch(Dispatchers.IO) {
                startDispatching()
            }
        } catch (e: Exception) {
            log.error(e) { "Error accepting connection" }
        }
    }

    private suspend fun startDispatching() {
        log.info { "Starting dispatcher job" }
        while (isActive) {
            try {
                dispatch()
            } catch (e: Exception) {
                log.error { "Dispatch error: ${e.message}" }
            }
        }
        log.info { "Closing dispatcher job" }
    }

    fun close() {
        nioServer.closeConnection(this)
    }

}

class TestLoginConnection(
    serverSocket: ServerSocket,
    nioServer: TestServer
) : TestConnection(serverSocket, nioServer) {

    override suspend fun dispatch() {
        input?.readUTF8Line()?.also { line ->
            when (line.toLowerCase()) {
                "exit" -> {
                    log.info { "Exit msg received" }
                    nioServer.closeConnection(this)
                }
                else -> log.info { "Msg received: $line" }
            }
        }
    }

}