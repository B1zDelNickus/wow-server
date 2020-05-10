package com.avp.wow.network.client.factories

import com.avp.wow.network.client.game.GameServerConnection.Companion.State
import com.avp.wow.network.client.game.GameServerInputPacket
import com.avp.wow.network.client.game.GameServerInputPacketHandler
import com.avp.wow.network.client.game.input.InAuthClientOk
import com.avp.wow.network.client.game.input.InClientLoginCheckResponse
import com.avp.wow.network.client.game.input.InInitSession
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
object GameServerInputPacketFactory {

    val packetHandler = GameServerInputPacketHandler()

    init {
        /**
         * Main packets
         */
        addPacket(InInitSession(State.CONNECTED)) // 5.1
        addPacket(InAuthClientOk(State.CONNECTED)) // 5.1
        addPacket(InClientLoginCheckResponse(State.CONNECTED)) // 5.1
        //addPacket(CM_L2AUTH_LOGIN_CHECK(0x015F, State.CONNECTED)) // 5.1

    }

    private fun addPacket(prototype: GameServerInputPacket) {
        packetHandler.addPacketPrototype(prototype)
    }

}