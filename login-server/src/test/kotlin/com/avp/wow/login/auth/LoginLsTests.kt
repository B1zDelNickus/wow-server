package com.avp.wow.login.auth

import com.avp.wow.game.network.GameNioServer
import com.avp.wow.game.network.client.GameClientConnectionFactory
import com.avp.wow.game.network.ls.GameLsConnectionFactory
import com.avp.wow.login.network.LoginNioServer
import com.avp.wow.login.network.client.LoginClientConnectionFactory
import com.avp.wow.login.network.gs.LoginGsConnectionFactory
import com.avp.wow.network.KtorConnectionConfig
import com.avp.wow.network.NetworkConstants.DEFAULT_LOGIN_SERVER_CLIENT_HOST
import com.avp.wow.network.NetworkConstants.DEFAULT_LOGIN_SERVER_CLIENT_PORT
import com.avp.wow.network.NetworkConstants.DEFAULT_LOGIN_SERVER_GS_HOST
import com.avp.wow.network.NetworkConstants.DEFAULT_LOGIN_SERVER_GS_PORT
import com.avp.wow.network.client.KtorNioClient
import com.avp.wow.network.client.login.LoginServerConnectionFactory
import com.avp.wow.network.ncrypt.KeyGen
import io.kotlintest.specs.StringSpec
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.delay

@KtorExperimentalAPI
class LoginLsTests : StringSpec({

    "login with encryption test" {

        KeyGen.init()

        val loginServerClientConfig = KtorConnectionConfig(
            hostName = DEFAULT_LOGIN_SERVER_CLIENT_HOST,
            port = DEFAULT_LOGIN_SERVER_CLIENT_PORT,
            connectionName = "Test Login Client Connection",
            factory = LoginClientConnectionFactory()
        )

        val loginServerGsConfig = KtorConnectionConfig(
            hostName = DEFAULT_LOGIN_SERVER_GS_HOST,
            port = DEFAULT_LOGIN_SERVER_GS_PORT,
            connectionName = "Test Login GS Connection",
            factory = LoginGsConnectionFactory()
        )

        val loginServer = LoginNioServer(
            serverConfigs = listOf(
                loginServerClientConfig,
                loginServerGsConfig
            )
        )

        loginServer.start()

        delay(3_000)

        val gameServerClientConfig = KtorConnectionConfig(
            hostName = "*",
            port = 2323,
            connectionName = "",
            factory = GameClientConnectionFactory()
        )

        val gameServerLsConfig = KtorConnectionConfig(
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

        val clientLsConfig = KtorConnectionConfig(
            hostName = DEFAULT_LOGIN_SERVER_CLIENT_HOST,
            port = DEFAULT_LOGIN_SERVER_CLIENT_PORT,
            connectionName = "Test Login Server Connection",
            factory = LoginServerConnectionFactory()
        )

        val client = KtorNioClient(
            clientLsConfig = clientLsConfig
        ).apply {

            start()

            delay(1_000) // wait auth gg operations

            login(login = "admin", password = "admin")

        }

        delay(1_000)

        //client.shutdown()
        //gameServer.shutdown()
        //loginServer.shutdown()

        delay(2 * 1_000)

    }

})