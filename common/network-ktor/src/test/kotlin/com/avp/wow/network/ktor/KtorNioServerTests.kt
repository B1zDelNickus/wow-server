package com.avp.wow.network.ktor

import com.avp.wow.network.KtorConnectionConfig
import com.avp.wow.network.NetworkConstants.DEFAULT_LOGIN_SERVER_HOST
import com.avp.wow.network.NetworkConstants.DEFAULT_LOGIN_SERVER_PORT
import com.avp.wow.network.client.KtorNioClient
import com.avp.wow.network.client.login.LoginServerConnectionFactory
import com.avp.wow.network.ktor.login.client.LoginClientConnectionFactory
import com.avp.wow.network.ncrypt.KeyGen
import io.kotlintest.specs.StringSpec
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay

@KtorExperimentalAPI
class KtorNioServerTests : StringSpec({

    "test" {

        KeyGen.init()

        val host = DEFAULT_LOGIN_SERVER_HOST
        val port = DEFAULT_LOGIN_SERVER_PORT

        val loginServerConfig = KtorConnectionConfig(
            hostName = host,
            port = port,
            connectionName = "Test Login Connection",
            factory = LoginClientConnectionFactory()
        )

        //val client = SimpleTcpClient(host, port, true, Dispatchers.IO)

        val server = KtorNioServer(
            serverConfigs = listOf(
                loginServerConfig
            ),
            context = Dispatchers.IO
        )

        server.connect()

        delay(3000)

        KtorNioClient(
            loginServerConfig = KtorConnectionConfig(
                hostName = DEFAULT_LOGIN_SERVER_HOST,
                port = DEFAULT_LOGIN_SERVER_PORT,
                connectionName = "Test Login Server Connection",
                factory = LoginServerConnectionFactory()
            )
        ).apply {

            connect()

            delay(1_000) // wait auth gg operations

            login(login = "admin", password = "admin")

        }

        delay(15_000)

        server.shutdown()

    }

}) {



}