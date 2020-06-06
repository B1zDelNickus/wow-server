package com.avp.wow.network.ktor

import com.avp.wow.network.KtorConnectionConfig
import com.avp.wow.network.NetworkConstants.DEFAULT_LOGIN_SERVER_CLIENT_HOST
import com.avp.wow.network.NetworkConstants.DEFAULT_LOGIN_SERVER_CLIENT_PORT
import com.avp.wow.network.NetworkConstants.DEFAULT_LOGIN_SERVER_GS_HOST
import com.avp.wow.network.NetworkConstants.DEFAULT_LOGIN_SERVER_GS_PORT
import com.avp.wow.network.client.KtorNioClient
import com.avp.wow.network.client.game.output.activity.OutActivity1
import com.avp.wow.network.client.login.LoginServerConnectionFactory
import com.avp.wow.network.ktor.game.GameNioServer
import com.avp.wow.network.ktor.game.client.GameClientConnectionFactory
import com.avp.wow.network.ktor.game.ls.GameLsConnectionFactory
import com.avp.wow.network.ktor.login.LoginNioServer
import com.avp.wow.network.ktor.login.client.LoginClientConnectionFactory
import com.avp.wow.network.ktor.login.gs.LoginGsConnectionFactory
import com.avp.wow.network.ncrypt.KeyGen
import io.kotlintest.specs.StringSpec
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.*
import java.lang.Error
import java.util.concurrent.atomic.AtomicLong

@KtorExperimentalAPI
class NioServerStressTests : StringSpec({

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

        val clientsConnected = AtomicLong(0)
        val clientsFailed = AtomicLong(0)


        val jobs = List(CLIENTS_NUMBER) { ind ->

            /*delay(CLIENTS_CONNECT_DELAY)

            if (ind % 100 == 0)
                delay(3_000)*/

            async {

                println("Starting client #$ind")

                var client: KtorNioClient? = null

                try {
                    client = KtorNioClient(
                        clientLsConfig = clientLsConfig
                    ).apply {

                        start()

                        delay(1_000) // wait auth gg operations

                        /*login(login = "user$ind", password = "admin")

                        delay(1_000)

                        enterGame()

                        delay(1_000)

                        repeat(PACKET_SEQ_COUNT) {

                            sendGamePacket(OutActivity1())

                            delay(PACKET_SEQ_DELAY)

                        }

                        delay(1_000)*/

                        //shutdown()

                        clientsConnected.incrementAndGet()

                    }
                } catch (e: Error) {
                    clientsFailed.incrementAndGet()
                } catch (e: Exception) {
                    clientsFailed.incrementAndGet()
                }

                client
            }

        }

        jobs.awaitAll()

        println("Active clients: ${gameServer.activeConnectionsCount}")

        /*val actJobs = jobs.awaitAll().filterNotNull().mapIndexed { index, cl ->
            launch {

                cl.apply {

                    login(login = "user$index", password = "admin")

                    delay(1_000)

                    enterGame()

                    delay(1_000)

                    repeat(PACKET_SEQ_COUNT) {

                        sendGamePacket(OutActivity1())

                        delay(PACKET_SEQ_DELAY)

                    }

                    delay(1_000)

                    shutdown()

                }

            }
        }

        actJobs.joinAll()*/

        //delay(TEST_DURATION_IN_MIN * 60 * 1_000)

        println("Clients done: ${clientsConnected.get()}")
        println("Clients failed: ${clientsFailed.get()}")

        gameServer.shutdown()
        loginServer.shutdown()

    }

}) {

    companion object {

        const val TEST_DURATION_IN_MIN = 10L
        const val CLIENTS_CONNECT_DELAY = 100L
        const val CLIENTS_NUMBER = 1_000
        const val PACKET_SEQ_COUNT = 1
        const val PACKET_SEQ_DELAY = 100L

    }

}