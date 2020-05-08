package com.avp.wow.network.ktor

import com.avp.wow.network.BaseNioService
import com.avp.wow.network.IKtorConnectionFactory
import com.avp.wow.network.KtorConnection
import com.avp.wow.network.KtorConnectionConfig
import com.avp.wow.network.NetworkConstants.DEFAULT_LOGIN_SERVER_CLIENT_HOST
import com.avp.wow.network.NetworkConstants.DEFAULT_LOGIN_SERVER_CLIENT_PORT
import com.avp.wow.network.NetworkConstants.DEFAULT_LOGIN_SERVER_GS_HOST
import com.avp.wow.network.NetworkConstants.DEFAULT_LOGIN_SERVER_GS_PORT
import com.avp.wow.network.client.KtorNioClient
import com.avp.wow.network.client.login.LoginServerConnectionFactory
import com.avp.wow.network.ktor.game.GameNioServer
import com.avp.wow.network.ktor.game.client.GameClientConnectionFactory
import com.avp.wow.network.ktor.login.client.LoginClientConnectionFactory
import com.avp.wow.network.ktor.login.gs.LoginGsConnectionFactory
import com.avp.wow.network.ncrypt.KeyGen
import io.kotlintest.specs.StringSpec
import io.ktor.network.sockets.Socket
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay

@KtorExperimentalAPI
class NioServerRunTests : StringSpec({

    "test" {

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
            ),
            context = Dispatchers.IO
        )

        loginServer.connect()

        delay(3_000)

        val gameServerClientConfig = KtorConnectionConfig(
            hostName = "*",
            port = 2323,
            connectionName = "",
            factory = GameClientConnectionFactory()
        )

        val gameServer = GameNioServer(
            hostName = DEFAULT_LOGIN_SERVER_GS_HOST,
            port = DEFAULT_LOGIN_SERVER_GS_PORT,
            gameClientConfig = gameServerClientConfig
        )

        gameServer.connect()

        delay(3_000)

        val client = KtorNioClient(
            loginServerConfig = KtorConnectionConfig(
                hostName = DEFAULT_LOGIN_SERVER_CLIENT_HOST,
                port = DEFAULT_LOGIN_SERVER_CLIENT_PORT,
                connectionName = "Test Login Server Connection",
                factory = LoginServerConnectionFactory()
            )
        ).apply {

            connect()

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