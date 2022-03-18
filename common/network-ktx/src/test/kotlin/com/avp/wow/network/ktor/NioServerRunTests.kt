package com.avp.wow.network.ktor

import com.avp.wow.network.KtxConnectionConfig
import com.avp.wow.network.NetworkConstants.DEFAULT_LOGIN_SERVER_CLIENT_HOST
import com.avp.wow.network.NetworkConstants.DEFAULT_LOGIN_SERVER_CLIENT_PORT
import com.avp.wow.network.NetworkConstants.DEFAULT_LOGIN_SERVER_GS_HOST
import com.avp.wow.network.NetworkConstants.DEFAULT_LOGIN_SERVER_GS_PORT
import com.avp.wow.network.client.KtxNioClient
import com.avp.wow.network.client.login.LoginServerConnectionFactory
import com.avp.wow.network.ktor.game.GameNioServer
import com.avp.wow.network.ktor.game.client.GameClientConnectionFactory
import com.avp.wow.network.ktor.game.ls.GameLsConnectionFactory
import com.avp.wow.network.ktor.login.client.LoginClientConnectionFactory
import com.avp.wow.network.ktor.login.gs.LoginGsConnectionFactory
import com.avp.wow.network.ncrypt.KeyGen
import io.kotlintest.specs.StringSpec
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay

@KtorExperimentalAPI
class NioServerRunTests : StringSpec({

    "test" {

        KeyGen.init()

        val loginServerClientConfig = KtxConnectionConfig(
            hostName = DEFAULT_LOGIN_SERVER_CLIENT_HOST,
            port = DEFAULT_LOGIN_SERVER_CLIENT_PORT,
            connectionName = "Test Login Client Connection",
            factory = LoginClientConnectionFactory()
        )

        val loginServerGsConfig = KtxConnectionConfig(
            hostName = DEFAULT_LOGIN_SERVER_GS_HOST,
            port = DEFAULT_LOGIN_SERVER_GS_PORT,
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

        delay(3_000)

        val gameServerClientConfig = KtxConnectionConfig(
            hostName = "*",
            port = 2323,
            connectionName = "",
            factory = GameClientConnectionFactory()
        )

        val gameServerLsConfig = KtxConnectionConfig(
            hostName = DEFAULT_LOGIN_SERVER_GS_HOST,
            port = DEFAULT_LOGIN_SERVER_GS_PORT,
            connectionName = "",
            factory = GameLsConnectionFactory()
        )

        val gameServer = GameNioServer(
            gameLsConfig = gameServerLsConfig,
            gameClientConfig = gameServerClientConfig
        )

        gameServer.start()

        delay(3_000)

        val clientLsConfig = KtxConnectionConfig(
            hostName = DEFAULT_LOGIN_SERVER_CLIENT_HOST,
            port = DEFAULT_LOGIN_SERVER_CLIENT_PORT,
            connectionName = "Test Login Server Connection",
            factory = LoginServerConnectionFactory()
        )

        val client = KtxNioClient(
            clientLsConfig = clientLsConfig
        ).apply {

            start()

            delay(1_000) // wait auth gg operations

            login(login = "admin", password = "admin")

        }

        delay(5 * 60 * 1_000)

        client.shutdown()
        gameServer.shutdown()
        loginServer.shutdown()

    }

}) {



}