package com.avp.wow.network.client

import com.avp.wow.network.BaseConnection
import com.avp.wow.network.BaseNioService
import com.avp.wow.network.KtxConnection
import com.avp.wow.network.KtxConnectionConfig
import com.avp.wow.network.client.login.LoginServerConnection
import com.avp.wow.network.client.login.output.OutLogin
import io.ktor.network.selector.ActorSelectorManager
import io.ktor.network.sockets.aSocket
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

@KtorExperimentalAPI
class KtxNioClient(
    private val clientLsConfig: KtxConnectionConfig,
    context: CoroutineContext = Dispatchers.IO
) : BaseNioService() {

    override val scope by lazy { CoroutineScope(SupervisorJob() + context) }

    private val selector by lazy { ActorSelectorManager(scope.coroutineContext) }

    private var loginServerConnection: KtxConnection? = null
    private var gameServerConnection: KtxConnection? = null

    override val activeConnectionsCount: Int
        get() = listOfNotNull(clientLsConfig, gameServerConnection).size

    /**
     * Open connections to configured servers
     */
    @Throws(Error::class)
    override fun start() {
        /**
         * Main connect coroutine
         */
        scope.launch {

            /**
             * Create non-blocking socket channel for clients
             */
            try {

                log.info { "Connecting to Login Server - $clientLsConfig" }

                val socket = openSocket(
                    host = clientLsConfig.hostName,
                    port = clientLsConfig.port
                )

                log.info { "Connected to ${socket.remoteAddress}" }

                loginServerConnection =
                    clientLsConfig.factory.create(
                        socket = socket,
                        nio = this@KtxNioClient,
                        context = scope.coroutineContext
                    )

                launch {

                    /**
                     * Run Read Dispatcher coroutine
                     */
                    launch { loginServerConnection?.startReadDispatching() }

                    /**
                     * Run Write Dispatcher coroutine
                     */
                    launch { loginServerConnection?.startWriteDispatching() }

                    loginServerConnection?.initialized()

                }

            } catch (e: Exception) {
                log.error(e) { "Error while connection to login server." }
                throw Error(e)
            }

        }
    }

    fun connectGameServer(gameServerConfig: KtxConnectionConfig) {

        try {
            scope.launch {

                log.info { "Connecting to Game Server - $gameServerConfig" }

                val socket = openSocket(
                    host = gameServerConfig.hostName,
                    port = gameServerConfig.port
                )

                log.info { "Connected to ${socket.remoteAddress}" }

                gameServerConnection =
                    gameServerConfig.factory.create(
                        socket = socket,
                        nio = this@KtxNioClient,
                        context = scope.coroutineContext
                    )

                launch {

                    /**
                     * Run Read Dispatcher coroutine
                     */
                    launch { gameServerConnection?.startReadDispatching() }

                    /**
                     * Run Write Dispatcher coroutine
                     */
                    launch { gameServerConnection?.startWriteDispatching() }

                    gameServerConnection?.initialized()

                }

            }
        } catch (e: Exception) {
            log.error(e) { "Error while connection to game server." }
            throw Error(e)
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

    fun login(login: String, password: String) {
        (loginServerConnection as? LoginServerConnection)?.let {
            it.sendPacket(OutLogin(login = login, password = password, server = it))
        }
    }

    private suspend fun openSocket(host: String, port: Int) = aSocket(selector = selector)
        .tcp()
        .connect(
            hostname = host,
            port = port
        )

}