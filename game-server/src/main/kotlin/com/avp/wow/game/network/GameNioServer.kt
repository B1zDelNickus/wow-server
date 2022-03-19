package com.avp.wow.game.network

import com.avp.wow.game.network.client.GameClientConnection
import com.avp.wow.game.network.client.output.OutExitWorld
import com.avp.wow.game.network.ls.GameLsConnection
import com.avp.wow.network.BaseConnection
import com.avp.wow.network.BaseNioService
import com.avp.wow.network.KtorConnectionConfig
import io.ktor.network.selector.ActorSelectorManager
import io.ktor.network.sockets.aSocket
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.*
import java.net.InetSocketAddress
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.coroutines.CoroutineContext

@KtorExperimentalAPI
class GameNioServer(
    private val gameLsConfig: KtorConnectionConfig,
    private val gameClientConfig: KtorConnectionConfig,
    context: CoroutineContext = Dispatchers.IO
) : BaseNioService() {

    override val scope by lazy { CoroutineScope(SupervisorJob() + context) }

    private val selector by lazy { ActorSelectorManager(context = scope.coroutineContext) }

    var loginServerConnection: GameLsConnection? = null
    private val connections = CopyOnWriteArrayList<GameClientConnection>()

    /**
     * List of connections that should be closed by this `Dispatcher` as soon as possible.
     */
    private val pendingClose = CopyOnWriteArrayList<BaseConnection<*>>()

    private var processPendingClosing = false
    private var pendingShutdown = false

    override val activeConnectionsCount: Int
        get() = connections.size + listOfNotNull(loginServerConnection).size

    var clientPort = 0
    var connectedToLs = false

    override fun start() {

        /**
         * Start connection to Login Server
         */
        connectLs()

        /**
         * Start listening for Client connections
         */
        startClientListener()

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
        pendingShutdown = true
    }

    override fun notifyClose() {
        connections
            .forEach { conn ->
                conn.onServerClose()
            }
        loginServerConnection?.onServerClose()
    }

    override fun closeAll() {
        connections
            .forEach { conn ->
                conn.close(true)
            }
        loginServerConnection?.close(true)
        processPendingClosing = false
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
        when (connection) {
            loginServerConnection -> loginServerConnection = null
            else -> connections.remove(connection)
        }
    }

    fun connectLs() {

        connectedToLs = false

        if (pendingShutdown) return

        /**
         * Start connection to Login Server
         */
        scope.launch {

            while (!pendingShutdown) {

                try {

                    val loginSocket = aSocket(selector = selector)
                        .tcp()
                        .connect(
                            hostname = gameLsConfig.hostName,
                            port = gameLsConfig.port
                        )

                    log.info { "Connected to Login Server by address: ${loginSocket.remoteAddress}" }

                    loginServerConnection = gameLsConfig.factory.create(
                        socket = loginSocket,
                        nio = this@GameNioServer,
                        context = scope.coroutineContext
                    ) as GameLsConnection

                    launch { loginServerConnection?.startReadDispatching() }

                    launch { loginServerConnection?.startWriteDispatching() }

                    loginServerConnection?.initialized()

                    connectedToLs = true

                    break

                } catch (e: Exception) {

                    log.info { "Can't connect to LS: ${e.message}" }

                }

                delay(5_000)

            }

            log.info { "Successfully connected to LS!" }
        }

    }

    private fun startClientListener() {

        scope.launch {

            val isa = when (gameClientConfig.hostName) {
                "*" -> {
                    log.info { "Server listening on all available IPs on Port " + gameClientConfig.port.toString() + " for " + gameClientConfig.connectionName }
                    InetSocketAddress(gameClientConfig.port)
                }
                else -> {
                    log.info { "Server listening on IP: " + gameClientConfig.hostName + " Port " + gameClientConfig.port + " for " + gameClientConfig.connectionName }
                    InetSocketAddress(gameClientConfig.hostName, gameClientConfig.port)
                }
            }

            clientPort = gameClientConfig.port

            val gameServer = aSocket(selector = selector)
                .tcp()
                .bind(isa)

            while (!pendingShutdown) { // TODO replace with synchronized guard

                try {

                    val clientSocket = gameServer.accept()

                    log.info { "Accepted connection from client: ${clientSocket.remoteAddress}" }

                    val connection =
                        gameClientConfig.factory.create(
                            socket = clientSocket,
                            nio = this@GameNioServer,
                            context = scope.coroutineContext
                        ) as GameClientConnection

                    connections.add(connection)

                    launch { connection.startReadDispatching() }

                    launch { connection.startWriteDispatching() }

                    connection.initialized()

                    delay(25)

                } catch (e: Exception) {
                    log.info { "Error while accepting connection from client: ${e.message}" }
                }

            }

        }

    }

    fun kickAllClientsFromServer() {
        connections
            .forEach { con ->
                con.close(closePacket = OutExitWorld(), forced = true)
            }
    }

}