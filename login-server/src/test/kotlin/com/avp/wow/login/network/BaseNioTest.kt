package com.avp.wow.login.network

import com.avp.wow.game.network.GameNioServer
import com.avp.wow.game.network.client.GameClientConnectionFactory
import com.avp.wow.game.network.ls.GameLsConnectionFactory
import com.avp.wow.login.network.client.LoginClientConnectionFactory
import com.avp.wow.login.network.gs.LoginGsConnectionFactory
import com.avp.wow.network.KtorConnectionConfig
import com.avp.wow.network.NetworkConstants
import com.avp.wow.network.client.KtorNioClient
import com.avp.wow.network.client.login.LoginServerConnectionFactory
import com.avp.wow.network.ncrypt.KeyGen
import io.kotlintest.TestCase
import io.kotlintest.TestResult
import io.kotlintest.specs.AbstractStringSpec
import io.kotlintest.specs.StringSpec
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
abstract class BaseNioTest(body: AbstractStringSpec.() -> Unit) : StringSpec(body = body) {

    init {
        KeyGen.init()
    }

    override fun afterTest(testCase: TestCase, result: TestResult) {
        client.shutdown()
        gameServer.shutdown()
        loginServer.shutdown()
    }

    @KtorExperimentalAPI
    companion object {

        const val START_TIMEOUT = 200L
        const val PACKET_PROCESS_TIMEOUT = 500L

        private val loginServerClientConfig = KtorConnectionConfig(
            hostName = NetworkConstants.DEFAULT_LOGIN_SERVER_CLIENT_HOST,
            port = NetworkConstants.DEFAULT_LOGIN_SERVER_CLIENT_PORT,
            connectionName = "Test Login Client Connection",
            factory = LoginClientConnectionFactory()
        )

        private val loginServerGsConfig = KtorConnectionConfig(
            hostName = NetworkConstants.DEFAULT_LOGIN_SERVER_GS_HOST,
            port = NetworkConstants.DEFAULT_LOGIN_SERVER_GS_PORT,
            connectionName = "Test Login GS Connection",
            factory = LoginGsConnectionFactory()
        )

        val loginServer by lazy {
            LoginNioServer(
                serverConfigs = listOf(
                    loginServerClientConfig,
                    loginServerGsConfig
                )
            )
        }

        private val gameServerClientConfig = KtorConnectionConfig(
            hostName = "*",
            port = 2323,
            connectionName = "",
            factory = GameClientConnectionFactory()
        )

        private val gameServerLsConfig = KtorConnectionConfig(
            hostName = NetworkConstants.DEFAULT_LOGIN_SERVER_GS_HOST,
            port = NetworkConstants.DEFAULT_LOGIN_SERVER_GS_PORT,
            connectionName = "",
            factory = GameLsConnectionFactory()
        )

        val gameServer by lazy {
            GameNioServer(
                gameLsConfig = gameServerLsConfig,
                gameClientConfig = gameServerClientConfig
            )
        }

        private val clientLsConfig = KtorConnectionConfig(
            hostName = NetworkConstants.DEFAULT_LOGIN_SERVER_CLIENT_HOST,
            port = NetworkConstants.DEFAULT_LOGIN_SERVER_CLIENT_PORT,
            connectionName = "Test Login Server Connection",
            factory = LoginServerConnectionFactory()
        )

        val client by lazy {
            KtorNioClient(
                clientLsConfig = clientLsConfig
            )
        }

    }

}