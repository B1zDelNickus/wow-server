package com.avp.wow.login.network

import com.avp.wow.network.BaseConnection
import com.avp.wow.network.BaseNioService
import com.avp.wow.network.KtorConnection
import com.avp.wow.network.KtorConnectionConfig
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.util.*
import kotlinx.coroutines.*
import mu.KotlinLogging
import java.net.InetSocketAddress
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.coroutines.CoroutineContext

@KtorExperimentalAPI
class LoginNioServer(
    private val serverConfigs: List<KtorConnectionConfig> = emptyList(),
    context: CoroutineContext = Dispatchers.IO
) : BaseNioService() {

    override val log = KotlinLogging.logger(this::class.java.name)

    override val scope by lazy { CoroutineScope(SupervisorJob() + context) }

    private val servers = CopyOnWriteArrayList<ServerSocket>()
    val connections = CopyOnWriteArrayList<KtorConnection<*>>()

    /**
     * List of connections that should be closed by this `Dispatcher` as soon as possible.
     */
    private val pendingClose = CopyOnWriteArrayList<BaseConnection<*>>()

    private var processPendingClosing = false

    private var listenerJob: Job? = null

    /**
     * Open connections to configured servers
     */
    @Throws(Error::class)
    override fun start() {


        /**
         * Create non-blocking socket channel for clients
         */
        try {

            log.info { "Connecting to servers..." }
            /**
             * Main connect coroutine
             */
            listenerJob = scope.launch {

                /**
                 * Bind the server socket to the specified address and port
                 */
                serverConfigs.forEach { cfg ->

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

                    val server = aSocket(ActorSelectorManager(coroutineContext))
                        .tcp()
                        .bind(isa)
                        .also { servers.add(it) }

                    /**
                     * Run Accept Dispatcher coroutine
                     */
                    launch {

                        while (true) { // TODO replace with synchronized guard

                            /**
                             * Accept socket connection from clients
                             */
                            val socket = server.accept()

                            log.info { "Client socket accepted: ${socket.remoteAddress} to '${cfg.connectionName}'" }

                            /**
                             * Run Accepted Connection coroutine with R\W dispatching
                             */
                            //launch {

                            /**
                             * Create connection object and pass it to NIO server infrastructure
                             */
                            cfg.factory.create(
                                socket = socket,
                                nio = this@LoginNioServer,
                                context = scope.coroutineContext
                            )
                                .also { conn ->
                                    /**
                                     * Add new connection to nio's all-connections pool
                                     */
                                    connections.add(conn)

                                    /**
                                     * Run Read Dispatcher coroutine
                                     */
                                    launch { conn.startReadDispatching() }

                                    /**
                                     * Run Write Dispatcher coroutine
                                     */
                                    launch { conn.startWriteDispatching() }

                                    conn.initialized()
                                }
                            //}

                            delay(25)

                        }

                    }

                }

                log.info { "Connected to servers: \n${serverConfigs.joinToString("\n") { "\t\t### address: ${it.hostName}:${it.port} | name: ${it.connectionName} ###" }}" }

            }

        } catch (e: Exception) {
            log.error(e) { "Error occurred while connecting servers: ${e.message}" }
            throw Error("Error initialize LoginNioServer")
        }

        /**
         * Coroutine for closing pending connections
         */
        scope.launch {

            processPendingClosing = true

            while (processPendingClosing) {
                processPendingClose()
            }

        }
    }

    override fun closeChannels() {
        listenerJob?.cancel()
        listenerJob = null
    }

    /**
     * Calls onServerClose method for all active connections.
     */
    override fun notifyClose() {
        connections
            .forEach { conn ->
                conn.onServerClose()
            }
    }

    /**
     * Close all active connections.
     */
    override fun closeAll() {
        connections
            .forEach { conn ->
                conn.close(true)
            }
    }

    /**
     * Process Pending Close connections.
     */
    private fun processPendingClose() {
        synchronized(pendingClose) {
            for (connection in pendingClose) {
                connection.closeConnectionImpl()
            }
            pendingClose.clear()
        }
    }

    override fun closeConnection(connection: BaseConnection<*>) {
        synchronized(pendingClose) {
            pendingClose.add(connection)
        }
    }

    override fun removeConnection(connection: BaseConnection<*>) {
        connections.remove(connection)
    }

    /**
     * @return Number of active connections.
     */
    override val activeConnectionsCount get() = connections.size

    companion object {

    }

}