package com.avp.wow.network.ktx

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
                    hostName = "127.0.0.1",
                    port = 2233,
                    connectionName = "Test Login Connection",
                    factory = LoginConnectionFactory()
                )
            ),
            readWriteThreads = 8,
            context = Dispatchers.IO
        )

        server.connect()

        delay(2000)

        server.shutdown()

    }

})