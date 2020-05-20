package com.avp.wow.game.network.factories

import com.avp.wow.game.network.client.GameClientConnection.Companion.State
import com.avp.wow.game.network.client.GameClientInputPacket
import com.avp.wow.game.network.client.GameClientInputPacketHandler
import com.avp.wow.game.network.client.input.InAuthClient
import com.avp.wow.game.network.client.input.InClientLoginCheck
import com.avp.wow.game.network.client.input.InEnterWorld
import com.avp.wow.game.network.client.input.activity.*
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
object GameClientInputPacketFactory {

    val packetHandler = GameClientInputPacketHandler()

    init {
        /**
         * Test activity packets
         */
        addPacket(InActivity1(State.IN_GAME))
        addPacket(InActivity2(State.IN_GAME))
        addPacket(InActivity3(State.IN_GAME))
        addPacket(InActivity4(State.IN_GAME))
        addPacket(InActivity5(State.IN_GAME))

        /**
         * Main packets
         */
        addPacket(InAuthClient(State.CONNECTED))
        addPacket(InClientLoginCheck(State.CONNECTED))
        addPacket(InEnterWorld(State.AUTHED))
        //addPacket(CM_L2AUTH_LOGIN_CHECK(0x015F, State.CONNECTED)) // 5.1

    }

    private fun addPacket(prototype: GameClientInputPacket) {
        packetHandler.addPacketPrototype(prototype)
    }

}