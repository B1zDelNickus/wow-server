package com.avp.wow.game.network

import com.avp.wow.game.network.ls.GameLsConnection
import com.avp.wow.network.BaseConnection
import com.avp.wow.network.BaseNioService
import com.avp.wow.network.KtorConnectionConfig
import io.ktor.network.selector.ActorSelectorManager
import io.ktor.network.sockets.aSocket
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.*
import java.net.InetSocketAddress
import kotlin.coroutines.CoroutineContext

@KtorExperimentalAPI
class GameNioServer(
    private val gameLsConfig: KtorConnectionConfig,
    private val gameClientConfig: KtorConnectionConfig,
    context: CoroutineContext = Dispatchers.IO
) : BaseNioService() {

    override val scope by lazy { CoroutineScope(SupervisorJob() + context) }

    var loginServerConnection: GameLsConnection? = null

    override val activeConnectionsCount: Int
        get() = TODO("Not yet implemented")

    var clientPort = 0

    override fun start() {

        scope.launch {

            try {

                val selector = ActorSelectorManager(context = scope.coroutineContext)

                /**
                 * Start connection to Login Server
                 */

                launch {

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

                }

                /**
                 * Start listening for Client connections
                 */

                launch {

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

                    launch {

                        while (true) { // TODO replace with syncronized guard

                            val clientSocket = gameServer.accept()

                            log.info { "Accepted connection from client: ${clientSocket.remoteAddress}" }

                            //launch {

                                val connection =
                                    gameClientConfig.factory.create(
                                        socket = clientSocket,
                                        nio = this@GameNioServer,
                                        context = scope.coroutineContext
                                    )

                                launch { connection.startReadDispatching() }

                                launch { connection.startWriteDispatching() }

                                connection.initialized()

                            //}

                            delay(25)

                        }

                    }

                }

            } catch (e: Exception) {
                log.error(e) { "Error occurred while connecting servers: ${e.message}" }
                throw Error("Error initialize GameNioServer")
            }

        }

    }

    override fun closeChannels() {
        TODO("Not yet implemented")
    }

    override fun notifyClose() {
        TODO("Not yet implemented")
    }

    override fun closeAll() {
        TODO("Not yet implemented")
    }

    override fun closeConnection(connection: BaseConnection) {
        TODO("Not yet implemented")
    }

    override fun removeConnection(connection: BaseConnection) {
        TODO("Not yet implemented")
    }

}