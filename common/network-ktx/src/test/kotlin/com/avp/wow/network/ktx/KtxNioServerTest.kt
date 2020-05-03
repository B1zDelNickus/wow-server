package com.avp.wow.network.ktx

import com.avp.wow.network.NetworkConstants.DEFAULT_LOGIN_SERVER_HOST
import com.avp.wow.network.NetworkConstants.DEFAULT_LOGIN_SERVER_PORT
import com.avp.wow.network.client.buildWowClient
import com.avp.wow.network.ktx.login.client.LoginConnectionFactory
import com.avp.wow.network.ncrypt.KeyGen
import io.kotlintest.specs.StringSpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

class KtxNioServerTest : StringSpec({

    "test" {

        runBlocking {
            KeyGen.init()
        }

        val server = KtxNioServer(
            serverConfigs = listOf(
                KtxServerConfig(
                    hostName = DEFAULT_LOGIN_SERVER_HOST,
                    port = DEFAULT_LOGIN_SERVER_PORT,
                    connectionName = "Test Login Connection",
                    factory = LoginConnectionFactory()
                )
            ),
            readWriteThreads = 8,
            context = Dispatchers.IO
        )

        server.connect()

        delay(100)

        buildWowClient {



        }

        delay(2000)

        server.shutdown()

    }

})