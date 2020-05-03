package com.avp.wow.network.ktor

import com.avp.wow.network.BaseNioServer
import io.ktor.network.selector.ActorSelectorManager
import io.ktor.network.sockets.ServerSocket
import io.ktor.network.sockets.aSocket
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.net.InetSocketAddress
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.coroutines.CoroutineContext

@KtorExperimentalAPI
class KtorNioServer(
    private val serverConfigs: List<KtorServerConfig> = emptyList(),
    context: CoroutineContext = Dispatchers.IO
) : BaseNioServer() {

    override val scope by lazy { CoroutineScope(SupervisorJob() + context) }

    private val servers = CopyOnWriteArrayList<ServerSocket>()
    private val connections = CopyOnWriteArrayList<KtorConnection>()

    /**
     * List of connections that should be closed by this `Dispatcher` as soon as possible.
     */
    private val pendingClose = CopyOnWriteArrayList<KtorConnection>()

    private var processPendingClosing = false

    /**
     * Open connections to configured servers
     */
    @Throws(Error::class)
    override fun connect() {

        /**
         * Main connect coroutine
         */
        scope.launch {

            /**
             * Create non-blocking socket channel for clients
             */
            try {

                log.info { "Connecting to servers..." }

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

                        /**
                         * Accept socket connection from clients
                         */
                        val socket = server.accept()

                        log.info { "Socket accepted: ${socket.remoteAddress} to '${cfg.connectionName}'" }

                        /**
                         * Create connection object and pass it to NIO server infrastructure
                         */
                        cfg.factory.create(socket = socket, nio = this@KtorNioServer)
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

                    }

                }

                log.info { "Connected to servers: \n${serverConfigs.joinToString("\n") { "\t\t### address: ${it.hostName}:${it.port} | name: ${it.connectionName} ###" }}" }

            } catch (e: Exception) {
                log.error(e) { "Error occurred while connecting servers: ${e.message}" }
                throw Error("Error initialize NioServer")
            }

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

    override fun closeChannels() = Unit

    /*override fun shutdown() {

        log.info { "Stopping NIO server..." }

        *//**
         * Sending DC packets for active clients
         *//*
        log.info { "Sending DC packets to clients and close connections..." }
        notifyServerClose()

        *//**
         * Wait 1s for coroutines to execute close operations
         *//*
        try {
            Thread.sleep(1000)
        } catch (t: Throwable) {
            log.warn(t) { "Nio thread was interrupted during shutdown" }
        }

        log.info { "Active connections: $getActiveConnections" }

        *//**
         * DC all
         *//*
        log.info { "Forced Disconnecting all connections..." }
        closeAll()

        *//**
         * Wait 1s for coroutines to execute close operations
         *//*
        try {
            Thread.sleep(1000)
        } catch (t: Throwable) {
            log.warn(t) { "Nio thread was interrupted during shutdown" }
        }

        log.info { "Active connections: $getActiveConnections" }

        servers.forEach { srv ->
            srv.close()
            srv.dispose()
        }
        servers.clear()

        connections.forEach { conn ->
            conn.socket.close()
            conn.socket.dispose()
        }
        connections.clear()

        processPendingClosing = false

        *//**
         * Wait 1s for coroutines to execute close operations
         *//*
        try {
            Thread.sleep(1000)
        } catch (t: Throwable) {
            log.warn(t) { "Nio thread was interrupted during shutdown" }
        }

        log.info { "NIO server has been stopped." }

        localScope.cancel()
    }*/

    /**
     * Calls onServerClose method for all active connections.
     */
    override fun notifyServerClose() {
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
                closeConnectionImpl(connection)
            }
            pendingClose.clear()
        }
    }

    /**
     * Connection will be closed [onlyClose()] and onDisconnect() method will be executed on another thread [DisconnectionThreadPool] after getDisconnectionDelay() time in ms. This method may only be called by current Dispatcher Thread.
     * @param con
     */
    fun closeConnectionImpl(connection: KtorConnection) {
        if (connection.onlyClose()) {
            scope.launch {
                connection.onDisconnect()
            }
        }
    }

    fun closeConnection(connection: KtorConnection) {
        synchronized(pendingClose) {
            pendingClose.add(connection)
        }
    }

    fun removeConnection(connection: KtorConnection) {
        connections.remove(connection)
    }

    /**
     * @return Number of active connections.
     */
    override val getActiveConnections get() = connections.size

    companion object {

    }

}