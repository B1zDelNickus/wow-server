package com.avp.wow.network.client

import com.avp.wow.network.*
import com.avp.wow.network.client.game.GameServerConnection
import com.avp.wow.network.client.game.GameServerOutputPacket
import com.avp.wow.network.client.game.SessionKey
import com.avp.wow.network.client.game.output.OutEnterWorld
import com.avp.wow.network.client.login.LoginServerConnection
import com.avp.wow.network.client.login.output.OutLogin
import io.ktor.network.selector.ActorSelectorManager
import io.ktor.network.sockets.aSocket
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import mu.KotlinLogging
import kotlin.coroutines.CoroutineContext

@KtorExperimentalAPI
class KtorNioClient(
    private val clientLsConfig: KtorConnectionConfig,
    context: CoroutineContext = Dispatchers.IO
) : BaseNioService() {

    override val log = KotlinLogging.logger(this::class.java.name)

    override val scope by lazy { CoroutineScope(SupervisorJob() + context) }

    private val selector by lazy { ActorSelectorManager(scope.coroutineContext) }

    var loginServerConnection: LoginServerConnection? = null
    var gameServerConnection: GameServerConnection? = null
    var sessionKey: SessionKey? = null

    override val activeConnectionsCount: Int
        get() = listOfNotNull(loginServerConnection, gameServerConnection).size

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

                val socket = aSocket(selector = selector)
                    .tcp()
                    .connect(
                        hostname = clientLsConfig.hostName,
                        port = clientLsConfig.port
                    )

                log.info { "Connected to ${socket.remoteAddress}" }

                loginServerConnection =
                    clientLsConfig.factory.create(
                        socket = socket,
                        nio = this@KtorNioClient,
                        context = scope.coroutineContext
                    ) as LoginServerConnection

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
                    .connect(
                        hostname = gameServerConfig.hostName,
                        port = gameServerConfig.port
                    )

                log.info { "Connected to ${socket.remoteAddress}" }

                gameServerConnection =
                    gameServerConfig.factory.create(
                        socket = socket,
                        nio = this@KtorNioClient,
                        context = scope.coroutineContext
                    ) as GameServerConnection

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

    override fun closeChannels() = Unit

    override fun notifyClose() {
        listOfNotNull(gameServerConnection, loginServerConnection)
            .forEach { conn ->
                conn.onServerClose()
            }
    }

    override fun closeAll() {
        listOfNotNull(gameServerConnection, loginServerConnection)
            .forEach { conn ->
                conn.close(true)
            }
    }

    override fun closeConnection(connection: BaseConnection<*>) {
        connection.closeConnectionImpl()
    }

    override fun removeConnection(connection: BaseConnection<*>) {
        when (connection) {
            is GameServerConnection -> gameServerConnection = null
            is LoginServerConnection -> loginServerConnection = null
        }
        log.info { "Disconnected! Some reconnecting stuff here..." }
    }

    fun login(login: String, password: String) {
        loginServerConnection?.let { ls ->
            ls.sendPacket(OutLogin(login = login, password = password, server = ls))
        }
    }

    fun enterGame() {
        gameServerConnection?.sendPacket(OutEnterWorld())
    }

    fun sendGamePacket(pck: GameServerOutputPacket) {
        gameServerConnection?.sendPacket(pck)
    }

}