package com.avp.wow.login.connect

import com.avp.wow.login.network.LoginNioServer
import com.avp.wow.login.network.client.LoginClientConnectionFactory
import com.avp.wow.login.network.gs.LoginGsConnectionFactory
import com.avp.wow.network.KtorConnectionConfig
import com.avp.wow.network.NetworkConstants
import com.avp.wow.network.client.KtorNioClient
import com.avp.wow.network.client.login.LoginServerConnectionFactory
import com.avp.wow.network.ncrypt.KeyGen
import io.kotlintest.specs.StringSpec
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay

@KtorExperimentalAPI
class LsClientCorrectShutdownTests : StringSpec({

    KeyGen.init()

    "f: shutdown ls and send goodbye packet to client test" {

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

        repeat(3) {

            delay(10)

            val clientLsConfig = KtorConnectionConfig(
                hostName = NetworkConstants.DEFAULT_LOGIN_SERVER_CLIENT_HOST,
                port = NetworkConstants.DEFAULT_LOGIN_SERVER_CLIENT_PORT,
                connectionName = "Test Login Server Connection",
                factory = LoginServerConnectionFactory()
            )

            val client = KtorNioClient(
                clientLsConfig = clientLsConfig
            )

            client.start()

        }

        delay(3_000)

        println("Active connections: ${loginServer.activeConnectionsCount}")

        loginServer.shutdown()

        delay(3_000)

    }

    "shutdown client and send goodbye packet to ls test" {

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

        val clientLsConfig = KtorConnectionConfig(
            hostName = NetworkConstants.DEFAULT_LOGIN_SERVER_CLIENT_HOST,
            port = NetworkConstants.DEFAULT_LOGIN_SERVER_CLIENT_PORT,
            connectionName = "Test Login Server Connection",
            factory = LoginServerConnectionFactory()
        )

        val client = KtorNioClient(
            clientLsConfig = clientLsConfig
        )

        client.start()

        delay(3_000)

        client.shutdown()

        delay(3_000)

    }

})