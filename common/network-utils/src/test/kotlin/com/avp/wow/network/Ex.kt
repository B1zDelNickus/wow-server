package com.avp.wow.network

import io.kotlintest.specs.StringSpec
import io.ktor.network.selector.ActorSelectorManager
import io.ktor.network.sockets.*
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.cio.write
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.*
import mu.KotlinLogging
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

@KtorExperimentalAPI
class Ex : StringSpec({

    "test" {

        val nio = NioServer(
            /*configs = listOf(
                ServerConfig("*", 2323, "Test Connection", object : ConnectionFactory {}),
                ServerConfig("127.0.0.1", 2324, "Test Connection 2", object : ConnectionFactory {})
            )*/
            //ServerConfig("*", 2323, "Test Connection", LoginConnectionFactory()),
            ServerConfig("127.0.0.1", 2324, "Test Connection 2", LoginConnectionFactory()),
            context = coroutineContext//Dispatchers.IO//
        )

        nio.connect()

        /*delay(3000)

        println("###")

        val socket = aSocket(ActorSelectorManager(Dispatchers.IO))
            .tcp()
            .connect(InetSocketAddress("127.0.0.1", 2324))

        val output = socket.openWriteChannel(autoFlush = true)
        output.write("hello\r\n")

        val socket2 = aSocket(ActorSelectorManager(Dispatchers.IO))
            .tcp()
            .connect(InetSocketAddress("127.0.0.1", 2324))

        val output2 = socket2.openWriteChannel(autoFlush = true)
        output2.write("hi!\r\n")

        val socket3 = aSocket(ActorSelectorManager(Dispatchers.IO))
            .tcp()
            .connect(InetSocketAddress("127.0.0.1", 2324))

        val output3 = socket3.openWriteChannel(autoFlush = true)
        output3.write("Aloha!\r\n")
        output3.write("exit\r\n")*/

        nio.shutdown()

    }

})

@KtorExperimentalAPI
class NioServer(
    vararg configs: ServerConfig,
    context: CoroutineContext? = null
) : CoroutineScope {

    private val log = KotlinLogging.logger(this::javaClass.name)

    override val coroutineContext by lazy { context ?: Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()).asCoroutineDispatcher() }

    private val configs = configs.toList()

    private val selector = ActorSelectorManager(coroutineContext)

    private lateinit var acceptDispatcher: Dispatcher

    private val connections = ConcurrentHashMap<String, Connection>()

    private val pendingClose = arrayListOf<Connection>()

    @Throws(Error::class)
    fun connect() = launch { // GlobalScope.launch(coroutineContext) { TODO "difference???"

        log.info { "Connecting to servers..." }

        try {

            /**
             * Create non-blocking socket channel for clients
             */
            configs.forEach { cfg ->

                //launch {

                    //delay(1000)

                    /**
                     * Bind the server socket to the specified address and port
                     */
                    val isa = when (cfg.hostName) {
                        "*" -> {
                            log.info { "Server listening on all available IPs on Port " + cfg.port.toString() + " for " + cfg.connectionName }
                            InetSocketAddress(cfg.port)
                        }
                        else -> {
                            log.info { "Server listening on IP: " + cfg.hostName + " Port " + cfg.port + " for " + cfg.connectionName }
                            InetSocketAddress(cfg.hostName, cfg.port)
                        }
                    }

                    val server = aSocket(selector)
                        .tcp()
                        .bind(isa)

                    launch {

                        while (true) {

                            val socket = server.accept()

                            log.info { "Socket accepted: ${socket.remoteAddress} to '${cfg.connectionName}'" }

                            val conn = cfg.factory.create(socket)
                                .apply { initialized() }

                            launch { conn.apply { startDispatching() } }

                            //socket.attachments["connection"] = conn
                            connections[conn.ip] = conn

                            /*launch {

                                val input = socket.openReadChannel()
                                val output = socket.openWriteChannel(autoFlush = true)

                                try {
                                    while (true) {
                                        val line = input.readUTF8Line()

                                        println("${socket.remoteAddress}: $line")

                                        if (line == "exit") {
                                            log.info { "Closing connection: ${socket.remoteAddress}" }
                                            connections.remove(socket.remoteAddress.toString())
                                            socket.close()
                                            break
                                        }

                                        output.write("$line\r\n")
                                    }
                                } catch (e: Throwable) {
                                    e.printStackTrace()
                                    connections.remove(socket.remoteAddress.toString())
                                    socket.close()
                                }

                            }*/

                        }

                    }

                //}

                log.info { "After connect to ${cfg.connectionName}" }

                /*val server = aSocket(selector)
            .tcp()
            .bind(isa)*/

                /*thread(start = true) {

            launch {

                while (true) {

                    val socket = server.accept()

                    launch {

                        val input = socket.openReadChannel()
                        val output = socket.openWriteChannel(autoFlush = false)

                        try {

                            while (true) {
                                val line = input.readUTF8Line()

                                println("${socket.remoteAddress}: $line")
                                output.write("$line\r\n")
                            }

                        } catch (e: Exception) {

                            socket.close()

                        }

                    }

                }
            }

        }*/

                /*val socket = server.accept()

        socket.openReadChannel().readUTF8Line()*/

            }

        } catch (e: Exception) {

            log.error(e) { "Error occurred while connecting servers: ${e.message}" }
            throw Error("Error initialize NioServer")

        }
    }

    private fun initDispatchers(readWriteThreads: Int = 0) = runBlocking {

        when {
            readWriteThreads < 0 -> {
                //acceptDispatcher = AcceptDispatcherImpl()
            }
            else -> {

            }
        }

    }

    fun shutdown() {
        /*try {
            context.cancel()
        } catch (e: Exception) {

        }*/

        try {
            /*connections.values.forEach { sock -> sock.close() }
            connections.clear()

            Thread.sleep(1000)

            coroutineContext.cancelChildren()
            cancel(message = "shutdown")*/

        } catch (e: Exception) {

        }

        log.info { "Closing ServerChannels..." }
        try {
            for (key in selector.provider.openSelector().selectedKeys()) {
                key.cancel()
            }
            log.info { "ServerChannel closed." }
        } catch (e: java.lang.Exception) {
            log.error(e) {"Error during closing ServerChannel, $e" }
        }
    }

    /**
     * Add connection to pendingClose list, so this connection will be closed by this `Dispatcher` as soon as possible.
     * @see com.aionemu.commons.network.Dispatcher.closeConnection
     */
    fun closeConnection(con: Connection) {
        synchronized(pendingClose) { pendingClose.add(con) }
    }

    /**
     * Process Pending Close connections.
     */
    private fun processPendingClose() {
        synchronized(pendingClose) {
            for (connection in pendingClose) {
                closeConnectionImpl(connection)
            }
            pendingClose.clear()
        }
    }

    /**
     * Connection will be closed [onlyClose()] and onDisconnect() method will be executed on another thread [DisconnectionThreadPool] after getDisconnectionDelay() time in ms. This method may only be called by current Dispatcher Thread.
     * @param con
     */
    private fun closeConnectionImpl(con: Connection) {
        if (con.onlyClose()) // dcPool.scheduleDisconnection(new DisconnectionTask(con), con.getDisconnectionDelay());
        {
            //dcPool.execute(DisconnectionTask(con))
            launch {

                // discon

            }
        }
    }

    companion object {

    }
}

val Socket.attachments: MutableMap<String, Any>
    get() = mutableMapOf()

data class ServerConfig(
    val hostName: String,
    val port: Int,
    val connectionName: String,
    val factory: ConnectionFactory
)

interface ConnectionFactory {

    @Throws(IOException::class)
    fun create(socket: Socket): Connection

}

class LoginConnectionFactory : ConnectionFactory {

    override fun create(socket: Socket): Connection {
        return LoginConnection(socket)
    }

}

abstract class Connection(
    protected val socket: Socket,
    readBufferSize: Int = DEFAULT_R_BUFFER_SIZE,
    writeBufferSize: Int = DEFAULT_W_BUFFER_SIZE
) {

    protected val log = KotlinLogging.logger(this.javaClass.name)

    protected val inputChannel by lazy { socket.openReadChannel() }
    protected val outputChannel by lazy { socket.openWriteChannel(autoFlush = true) }

    protected val readBuffer by lazy {
        ByteArray(size = readBufferSize)
            .apply {
                reverse()
            }
    }

    protected val writeBuffer by lazy {
        ByteArray(size = writeBufferSize)
            .apply {
                reverse()
            }
    }

    /**
     * @return IP address of this Connection.
     */
    val ip by lazy { socket.remoteAddress.toString() }

    /**
     * True if OnDisconnect() method should be called immediately after this connection was closed.
     */
    protected var isForcedClosing = false

    /**
     * True if this connection should be closed after sending last server packet.
     */
    protected var pendingClose = false

    /**
     * True if this connection is already closed.
     */
    protected var closed = false

    /**
     * Object on witch some methods are synchronized
     */
    protected val guard = Any()

    /**
     * @return True if this connection is pendingClose and not closed yet.
     */
    val isPendingClose get() = pendingClose && !closed

    /**
     * @return True if write to this connection is possible.
     */
    protected val isWriteDisabled get() = pendingClose || closed

    /**
     * Used only for PacketProcessor synchronization purpose
     */
    private var locked = false

    @Throws(IOException::class)
    abstract suspend fun dispatch()

    fun CoroutineScope.startDispatching() = launch(coroutineContext) {

        while (true) {

            try {

                dispatch()
                synchronized(guard) {

                }

            } catch (e: Exception) {
                log.error(e) { "Dispatcher error: ${e.message}" }
            }

        }

    }

    /**
     * Connection will be closed at some time [by Dispatcher Thread], after that onDisconnect() method will be called to clear all other things.
     * @param forced is just hint that getDisconnectionDelay() should return 0 so OnDisconnect() method will be called without any delay.
     */
    fun close(forced: Boolean = false) {
        synchronized(guard) {
            if (isWriteDisabled) {
                return
            }
            isForcedClosing = forced
            //getDispatcher().closeConnection(this)
            /*try {
                log.info { "CLosing socket from: ${socket.remoteAddress}" }
                socket.close()
            } catch (ignored: IOException) { }*/
        }
    }

    /**
     * This will only close the connection without taking care of the rest. May be called only by Dispatcher Thread. Returns true if connection was not closed before.
     * @return true if connection was not closed before.
     */
    fun onlyClose(): Boolean {
        synchronized(guard) {
            if (closed) {
                return false
            }
            try {
                if (!socket.isClosed) {
                    socket.close()
                    //key.attach(null)
                    //key.cancel()
                }
                closed = true
            } catch (ignored: IOException) {
            }
        }
        return true
    }

    /**
     * Used only for PacketProcessor synchronization purpose. Return true if locked successful - if wasn't locked before.
     * @return locked
     */
    open fun tryLockConnection(): Boolean {
        return if (locked) {
            false
        } else true.also { locked = it }
    }

    /**
     * Used only for PacketProcessor synchronization purpose. Unlock this connection.
     */
    open fun unlockConnection() {
        locked = false
    }

    /**
     * @param data
     * @return True if data was processed correctly, False if some error occurred and connection should be closed NOW.
     */
    protected abstract fun processData(data: ByteBuffer): Boolean

    /**
     * This method will be called by Dispatcher, and will be repeated till return false.
     * @param data
     * @return True if data was written to buffer, False indicating that there are not any more data to write.
     */
    protected abstract fun writeData(data: ByteBuffer): Boolean

    /**
     * Called when AConnection object is fully initialized and ready to process and send packets. It may be used as hook for sending first packet etc.
     */
    abstract fun initialized()

    /**
     * This method is called by Dispatcher when connection is ready to be closed.
     * @return time in ms after witch onDisconnect() method will be called.
     */
    protected abstract fun getDisconnectionDelay(): Long

    /**
     * This method is called by Dispatcher to inform that this connection was closed and should be cleared. This method is called only once.
     */
    protected abstract fun onDisconnect()

    /**
     * This method is called by NioServer to inform that NioServer is shouting down. This method is called only once.
     */
    protected abstract fun onServerClose()

    companion object {

        const val DEFAULT_R_BUFFER_SIZE = 8192 * 2
        const val DEFAULT_W_BUFFER_SIZE = 8192 * 2

    }
}

class LoginConnection(socket: Socket) : Connection(socket = socket) {

    override suspend fun dispatch() {
        val line = inputChannel.readUTF8Line()
        println("${socket.remoteAddress}: $line")
        outputChannel.write("$line\r\n")
        log.info { "Dispatched from: ${socket.remoteAddress}" }
    }

    override fun processData(data: ByteBuffer): Boolean {
        TODO("Not yet implemented")
    }

    override fun writeData(data: ByteBuffer): Boolean {
        TODO("Not yet implemented")
    }

    override fun initialized() {
        log.info { "Connection from ${socket.remoteAddress} successfully initialized." }
    }

    override fun getDisconnectionDelay(): Long {
        TODO("Not yet implemented")
    }

    override fun onDisconnect() {
        TODO("Not yet implemented")
    }

    override fun onServerClose() {
        TODO("Not yet implemented")
    }

    companion object {

        enum class State {
            /**
             * Means that client just connects
             */
            CONNECTED,

            /**
             * Means that clients GameGuard is authenticated
             */
            AUTHED_GG,

            /**
             * Means that client is logged in.
             */
            AUTHED_LOGIN
        }

    }

}


abstract class Dispatcher(
    val serverSocket: ServerSocket
) {

    protected val log = KotlinLogging.logger(this.javaClass.name)

    /**
     * Object on witch register vs selector.select are synchronized
     */
    private val gate = Any()

    @Throws(IOException::class)
    abstract fun dispatch()

    fun start() = runBlocking {

        while (true) {

            try {

                dispatch()
                synchronized(gate) {}

            } catch (e: Exception) {
                log.error(e) { "Dispatcher error: ${e.message}" }
            }

        }

    }

    fun accept() = runBlocking {
        try {
            serverSocket.accept()
        } catch (e: Exception) {
            log.error(e) { "Error while accepting connection: ${e.message}" }
        }
    }

}

class AcceptDispatcherImpl(
    serverSocket: ServerSocket
) : Dispatcher(serverSocket) {

    override fun dispatch() {
        accept()
    }

}