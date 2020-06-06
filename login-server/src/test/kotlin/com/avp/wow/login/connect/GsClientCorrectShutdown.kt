package com.avp.wow.login.connect

import com.avp.wow.game.network.GameNioServer
import com.avp.wow.game.network.client.GameClientConnectionFactory
import com.avp.wow.game.network.ls.GameLsConnectionFactory
import com.avp.wow.login.network.LoginNioServer
import com.avp.wow.login.network.client.LoginClientConnectionFactory
import com.avp.wow.login.network.gs.LoginGsConnectionFactory
import com.avp.wow.network.KtorConnectionConfig
import com.avp.wow.network.NetworkConstants
import com.avp.wow.network.client.KtorNioClient
import com.avp.wow.network.client.game.output.activity.OutActivity1
import com.avp.wow.network.client.login.LoginServerConnectionFactory
import com.avp.wow.network.ncrypt.KeyGen
import io.kotlintest.specs.StringSpec
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay

@KtorExperimentalAPI
class GsClientCorrectShutdown : StringSpec({

    KeyGen.init()

    "shutdown gs and send goodbye packet to client test" {

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

        val clientLsConfig = KtorConnectionConfig(
            hostName = NetworkConstants.DEFAULT_LOGIN_SERVER_CLIENT_HOST,
            port = NetworkConstants.DEFAULT_LOGIN_SERVER_CLIENT_PORT,
            connectionName = "Test Login Server Connection",
            factory = LoginServerConnectionFactory()
        )

        val client = KtorNioClient(
            clientLsConfig = clientLsConfig
        ).apply {

            start()

            delay(200) // wait auth gg operations

            login(login = "admin", password = "admin")

            delay(200)

            enterGame()

        }

        delay(3_000)

        gameServer.shutdown()

        delay(3_000)

    }

    "f:close packet send to client test" {

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

        val clientLsConfig = KtorConnectionConfig(
            hostName = NetworkConstants.DEFAULT_LOGIN_SERVER_CLIENT_HOST,
            port = NetworkConstants.DEFAULT_LOGIN_SERVER_CLIENT_PORT,
            connectionName = "Test Login Server Connection",
            factory = LoginServerConnectionFactory()
        )

        val client = KtorNioClient(
            clientLsConfig = clientLsConfig
        ).apply {

            start()

            delay(200) // wait auth gg operations

            login(login = "admin", password = "admin")

            delay(200)

            enterGame()

        }

        delay(3_000)

        println("Active connections: ${gameServer.activeConnectionsCount}")

        gameServer.kickAllClientsFromServer()

        delay(3_000)

        println("Active connections: ${gameServer.activeConnectionsCount}")

    }

})