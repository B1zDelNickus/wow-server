package com.avp.wow.network.client.factories

import com.avp.wow.network.client.game.GameServerConnection.Companion.State
import com.avp.wow.network.client.game.GameServerInputPacket
import com.avp.wow.network.client.game.GameServerInputPacketHandler
import com.avp.wow.network.client.game.input.*
import com.avp.wow.network.client.game.input.activity.*
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
object GameServerInputPacketFactory {

    val packetHandler = GameServerInputPacketHandler()

    init {
        /**
         * Test activity packets
         */
        addPacket(InActivity1(State.IN_GAME)) // 5.1
        addPacket(InActivity2(State.IN_GAME)) // 5.1
        addPacket(InActivity3(State.IN_GAME)) // 5.1
        addPacket(InActivity4(State.IN_GAME)) // 5.1
        addPacket(InActivity5(State.IN_GAME)) // 5.1

        /**
         * Main packets
         */
        addPacket(InInitSession(State.CONNECTED)) // 5.1
        addPacket(InAuthClientOk(State.CONNECTED)) // 5.1
        addPacket(InClientLoginCheckResponse(State.CONNECTED)) // 5.1
        addPacket(InEnterWorldOk(State.AUTHED)) // 5.1
        addPacket(InExitWord(State.AUTHED, State.IN_GAME)) // 5.1
        //addPacket(CM_L2AUTH_LOGIN_CHECK(0x015F, State.CONNECTED)) // 5.1

    }

    private fun addPacket(prototype: GameServerInputPacket) {
        packetHandler.addPacketPrototype(prototype)
    }

}