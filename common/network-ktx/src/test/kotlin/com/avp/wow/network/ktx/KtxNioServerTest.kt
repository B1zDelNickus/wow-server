package com.avp.wow.network.ktx

import com.avp.wow.network.KtorConnectionConfig
import com.avp.wow.network.KtxConnectionConfig
import com.avp.wow.network.NetworkConstants.DEFAULT_LOGIN_SERVER_CLIENT_HOST
import com.avp.wow.network.NetworkConstants.DEFAULT_LOGIN_SERVER_CLIENT_PORT
import com.avp.wow.network.client.KtorNioClient
import com.avp.wow.network.client.login.LoginServerConnectionFactory
import com.avp.wow.network.ktx.login.client.LoginClientConnectionFactory
import com.avp.wow.network.ncrypt.KeyGen
import io.kotlintest.specs.StringSpec
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

@KtorExperimentalAPI
class KtxNioServerTest : StringSpec({

    "test" {

        runBlocking {
            KeyGen.init()
        }

        val server = KtxNioServer(
            serverConfigs = listOf(
                KtxConnectionConfig(
                    hostName = DEFAULT_LOGIN_SERVER_CLIENT_HOST,
                    port = DEFAULT_LOGIN_SERVER_CLIENT_PORT,
                    connectionName = "Test Login Clients Connection",
                    factory = LoginClientConnectionFactory()
                )
            ),
            readWriteThreads = 8,
            context = Dispatchers.IO
        )

        server.start()

        delay(10_000)

        runBlocking {
            while (!server.isUp) {}
        }

        /*buildWowClient {



        }*/

        val client = KtorNioClient(
            loginServerConfig = KtorConnectionConfig(
                hostName = DEFAULT_LOGIN_SERVER_CLIENT_HOST,
                port = DEFAULT_LOGIN_SERVER_CLIENT_PORT,
                connectionName = "Test Login Server Connection",
                factory = LoginServerConnectionFactory()
            )
        )

        client.start()

        delay(3_000)

        server.shutdown()

    }

})