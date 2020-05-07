package com.avp.wow.network.ktor.game

import com.avp.wow.network.BaseNioService
import com.avp.wow.network.KtorConnection
import com.avp.wow.network.KtorConnectionConfig
import com.avp.wow.network.ktor.game.ls.GameLsConnection
import io.ktor.network.selector.ActorSelectorManager
import io.ktor.network.sockets.aSocket
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.net.InetSocketAddress
import kotlin.coroutines.CoroutineContext

@KtorExperimentalAPI
class GameNioServer(
    private val hostName: String,
    private val port: Int,
    private val gameServerConfig: KtorConnectionConfig,
    context: CoroutineContext = Dispatchers.IO
) : BaseNioService() {

    override val scope by lazy { CoroutineScope(SupervisorJob() + context) }

    var loginServerConnection: GameLsConnection? = null

    override val activeConnectionsCount: Int
        get() = TODO("Not yet implemented")

    override fun connect() {

        scope.launch {

            try {

                val selector = ActorSelectorManager(context = scope.coroutineContext)

                /**
                 * Start connection to Login Server
                 */

                launch {

                    val loginSocket = aSocket(selector = selector)
                        .tcp()
                        .connect(hostname = hostName, port = port)

                    log.info { "Connected to Login Server by address: ${loginSocket.remoteAddress}" }

                    loginServerConnection = GameLsConnection(socket = loginSocket, nio = this@GameNioServer)

                    //launch {

                        launch { loginServerConnection?.startReadDispatching() }

                        launch { loginServerConnection?.startWriteDispatching() }

                        loginServerConnection?.initialized()

                    //}

                }

                /**
                 * Start listening for Client connections
                 */

                launch {

                    val isa = when (gameServerConfig.hostName) {
                        "*" -> {
                            log.info { "Server listening on all available IPs on Port " + gameServerConfig.port.toString() + " for " + gameServerConfig.connectionName }
                            InetSocketAddress(gameServerConfig.port)
                        }
                        else -> {
                            log.info { "Server listening on IP: " + gameServerConfig.hostName + " Port " + gameServerConfig.port + " for " + gameServerConfig.connectionName }
                            InetSocketAddress(gameServerConfig.hostName, gameServerConfig.port)
                        }
                    }

                    val gameServer = aSocket(selector = selector)
                        .tcp()
                        .bind(isa)

                    launch {

                        val clientSocket = gameServer.accept()

                        log.info { "Accepted connection from client: ${clientSocket.remoteAddress}" }

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

    fun closeConnectionImpl(connection: KtorConnection) {
        if (connection.onlyClose()) {
            scope.launch {
                connection.onDisconnect()
            }
        }
    }
}