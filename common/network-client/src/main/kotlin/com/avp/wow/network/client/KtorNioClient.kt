package com.avp.wow.network.client

import com.avp.wow.network.BaseNioService
import com.avp.wow.network.KtorConnection
import com.avp.wow.network.KtorConnectionConfig
import com.avp.wow.network.client.login.LoginServerConnection
import com.avp.wow.network.client.login.output.OutLogin
import io.ktor.network.selector.ActorSelectorManager
import io.ktor.network.sockets.ServerSocket
import io.ktor.network.sockets.aSocket
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import mu.KotlinLogging
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.coroutines.CoroutineContext

@KtorExperimentalAPI
class KtorNioClient(
    private val loginServerConfig: KtorConnectionConfig,
    context: CoroutineContext = Dispatchers.IO
) : BaseNioService() {

    override val scope by lazy { CoroutineScope(SupervisorJob() + context) }

    private val selector by lazy { ActorSelectorManager(scope.coroutineContext) }

    private var loginServerConnection: KtorConnection? = null
    private var gameServerConnection: KtorConnection? = null

    override val activeConnectionsCount: Int
        get() = listOfNotNull(loginServerConfig, gameServerConnection).size

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

                log.info { "Connecting to Login Server - $loginServerConfig" }

                val socket = aSocket(selector = selector)
                    .tcp()
                    .connect(hostname = loginServerConfig.hostName, port = loginServerConfig.port)

                log.info { "Connected to ${socket.remoteAddress}" }

                loginServerConnection =
                    loginServerConfig.factory.create(socket = socket, nio = this@KtorNioClient)

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

    fun connectGameServer(gameServerConfig: KtorConnectionConfig) {

        try {
            scope.launch {

                log.info { "Connecting to Game Server - $gameServerConfig" }

                val socket = aSocket(selector = selector)
                    .tcp()
                    .connect(hostname = gameServerConfig.hostName, port = gameServerConfig.port)

                log.info { "Connected to ${socket.remoteAddress}" }

                gameServerConnection =
                    gameServerConfig.factory.create(socket = socket, nio = this@KtorNioClient)

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

    fun closeConnectionImpl(connection: KtorConnection) {
        // todo place into connection with syncronization
    }

    fun login(login: String, password: String) {
        (loginServerConnection as? LoginServerConnection)?.let {
            it.sendPacket(OutLogin(login = login, password = password, server = it))
        }
    }

}