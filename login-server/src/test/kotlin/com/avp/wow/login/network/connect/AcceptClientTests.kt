package com.avp.wow.login.network.connect

import com.avp.wow.login.network.BaseNioTest
import com.avp.wow.login.network.client.LoginClientConnection
import com.avp.wow.login.network.client.LoginClientOutputPacket
import com.avp.wow.login.network.client.input.InAuthClient
import com.avp.wow.login.network.client.output.OutAuthClientOk
import com.avp.wow.login.network.client.output.OutInitSession
import com.avp.wow.login.network.factories.LoginClientInputPacketFactory
import com.avp.wow.login.network.factories.LoginClientOutputPacketFactory
import com.avp.wow.network.client.factories.LoginServerInputPacketFactory
import com.avp.wow.network.client.factories.LoginServerOutputPacketFactory
import com.avp.wow.network.client.login.LoginServerConnection
import com.avp.wow.network.client.login.LoginServerInputPacket
import com.avp.wow.network.client.login.input.InInitSession
import com.avp.wow.network.client.login.output.OutAuthClient
import com.avp.wow.network.client.login.output.OutLogin
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.ktor.util.KtorExperimentalAPI
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.primaryConstructor

@KtorExperimentalAPI
class AcceptClientTests : BaseNioTest({

    "accept client test" {

        /**
         * Prepare stage
         */

        var receivedSessionId = 0
        var packetsReceived = 0

        val acceptSessionPacket = object : LoginServerInputPacket(
            OutInitSession.OP_CODE,
            listOf(LoginServerConnection.Companion.State.CONNECTED)
        ) {
            override fun readImpl() {
                receivedSessionId = readD()
            }

            override suspend fun runImpl(){
                packetsReceived++
            }

        }

        /**
         * Assign stage
         */

        LoginClientOutputPacketFactory.packetHandler
            .apply {
                clearPrototypes()
                addPacketPrototype(packetClass = OutInitSession::class, opcode = OutInitSession.OP_CODE)
            }

        LoginServerInputPacketFactory.packetHandler
            .apply {
                clearPrototypes()
                addPacketPrototype(acceptSessionPacket)
            }

        /**
         * Act stage
         */

        loginServer.start()
        delay(START_TIMEOUT) // some time for start
        client.start()
        delay(PACKET_PROCESS_TIMEOUT) // some time for accept

        /**
         * Assert stage
         */

        loginServer.activeConnectionsCount shouldBe 1
        receivedSessionId shouldNotBe 0
        receivedSessionId shouldBe (loginServer.connections.first() as LoginClientConnection).sessionId
        packetsReceived shouldBe 1


    }

    "f:verify sessionId after accept client connection" {

        /**
         * Prepare stage
         */

        var verified = false

        val successSessionVerification = object : LoginClientOutputPacket() {
            override fun writeImpl(con: LoginClientConnection) {
                verified = true
            }
        }

        /**
         * Assign stage
         */

        LoginClientOutputPacketFactory.packetHandler
            .apply {
                clearPrototypes()
                addPacketPrototype(packetClass = OutInitSession::class, opcode = OutInitSession.OP_CODE)
                //addPacketPrototype(packetClass = successSessionVerification::class, opcode = OutAuthClientOk.OP_CODE)
            }

        LoginServerInputPacketFactory.packetHandler
            .apply {
                clearPrototypes()
                addPacketPrototype(InInitSession(LoginServerConnection.Companion.State.CONNECTED))
            }

        LoginServerOutputPacketFactory.packetHandler
            .apply {
                clearPrototypes()
                addPacketPrototype(packetClass = OutAuthClient::class, opcode = OutAuthClient.OP_CODE)
            }

        LoginClientInputPacketFactory.packetHandler
            .apply {
                clearPrototypes()
                addPacketPrototype(InAuthClient(LoginClientConnection.Companion.State.CONNECTED))
            }

        /**
         * Act stage
         */

        loginServer.start()
        delay(START_TIMEOUT) // some time for start
        client.start()
        delay(PACKET_PROCESS_TIMEOUT) // some time for accept

        /**
         * Assert stage
         */

        loginServer.activeConnectionsCount shouldBe 1
        client.loginServerConnection?.sessionId shouldBe (loginServer.connections.first() as LoginClientConnection).sessionId

    }

})