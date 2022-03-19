package com.avp.wow.login.connect

import com.avp.wow.game.network.GameNioServer
import com.avp.wow.game.network.client.GameClientConnectionFactory
import com.avp.wow.game.network.ls.GameLsConnectionFactory
import com.avp.wow.login.network.LoginNioServer
import com.avp.wow.login.network.client.LoginClientConnectionFactory
import com.avp.wow.login.network.gs.LoginGsConnectionFactory
import com.avp.wow.network.KtorConnectionConfig
import com.avp.wow.network.NetworkConstants
import com.avp.wow.network.ncrypt.KeyGen
import io.kotlintest.specs.StringSpec
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import mu.KotlinLogging

@KtorExperimentalAPI
class LsGsCorrectShutdownTests : StringSpec({

    KeyGen.init()

    "shutdown ls and send goodbye packet to ga test" {

        val loginServerClientConfig = KtorConnectionConfig(
            hostName = NetworkConstants.DEFAULT_LOGIN_SERVER_CLIENT_HOST,
            port = NetworkConstants.DEFAULT_LOGIN_SERVER_CLIENT_PORT,
            connectionName = "Test Login Client Connection",
            factory = LoginClientConnectionFactory()
        )

        val loginServerGsConfig = KtorConnectionConfig(
            hostName = NetworkConstants.DEFAULT_LOGIN_SERVER_GS_HOST,
            port = NetworkConstants.DEFAULT_LOGIN_SERVER_GS_PORT,
            connectionName = "Test Login GS Connection",
            factory = LoginGsConnectionFactory()
        )

        val loginServer = LoginNioServer(
            serverConfigs = listOf(
                loginServerClientConfig,
                loginServerGsConfig
            ),
            context = Dispatchers.IO
        )

        loginServer.start()

        delay(1_000)

        log.info { "Active connections on Ls: ${loginServer.activeConnectionsCount}" }

        val gameServerClientConfig = KtorConnectionConfig(
            hostName = "*",
            port = 2323,
            connectionName = "",
            factory = GameClientConnectionFactory()
        )

        val gameServerLsConfig = KtorConnectionConfig(
            hostName = NetworkConstants.DEFAULT_LOGIN_SERVER_GS_HOST,
            port = NetworkConstants.DEFAULT_LOGIN_SERVER_GS_PORT,
            connectionName = "",
            factory = GameLsConnectionFactory()
        )

        val gameServer = GameNioServer(
            gameLsConfig = gameServerLsConfig,
            gameClientConfig = gameServerClientConfig
        )

        gameServer.start()

        delay(3_000)

        log.info { "Active connections on Ls: ${loginServer.activeConnectionsCount}" }

        loginServer.shutdown()

        delay(15_000)

        log.info { "Active connections on Ls: ${loginServer.activeConnectionsCount}" }

    }

    "f:shutdown gs and send goodbye packet to ls test" {

        val loginServerClientConfig = KtorConnectionConfig(
            hostName = NetworkConstants.DEFAULT_LOGIN_SERVER_CLIENT_HOST,
            port = NetworkConstants.DEFAULT_LOGIN_SERVER_CLIENT_PORT,
            connectionName = "Test Login Client Connection",
            factory = LoginClientConnectionFactory()
        )

        val loginServerGsConfig = KtorConnectionConfig(
            hostName = NetworkConstants.DEFAULT_LOGIN_SERVER_GS_HOST,
            port = NetworkConstants.DEFAULT_LOGIN_SERVER_GS_PORT,
            connectionName = "Test Login GS Connection",
            factory = LoginGsConnectionFactory()
        )

        val loginServer = LoginNioServer(
            serverConfigs = listOf(
                loginServerClientConfig,
                loginServerGsConfig
            ),
            context = Dispatchers.IO
        )

        loginServer.start()

        delay(1_000)

        val gameServerClientConfig = KtorConnectionConfig(
            hostName = "*",
            port = 2323,
            connectionName = "",
            factory = GameClientConnectionFactory()
        )

        val gameServerLsConfig = KtorConnectionConfig(
            hostName = NetworkConstants.DEFAULT_LOGIN_SERVER_GS_HOST,
            port = NetworkConstants.DEFAULT_LOGIN_SERVER_GS_PORT,
            connectionName = "",
            factory = GameLsConnectionFactory()
        )

        val gameServer = GameNioServer(
            gameLsConfig = gameServerLsConfig,
            gameClientConfig = gameServerClientConfig
        )

        gameServer.start()

        delay(3_000)

        gameServer.shutdown()

        delay(3_000)

    }

}) {

    companion object {
        private val log = KotlinLogging.logger("com.avp.test-logger")
    }

}