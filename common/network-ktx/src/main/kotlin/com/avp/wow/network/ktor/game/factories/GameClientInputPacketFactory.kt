package com.avp.wow.network.ktor.game.factories

import com.avp.wow.network.ktor.game.client.GameClientConnection.Companion.State
import com.avp.wow.network.ktor.game.client.GameClientInputPacket
import com.avp.wow.network.ktor.game.client.GameClientInputPacketHandler
import com.avp.wow.network.ktor.game.client.input.InAuthClient
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
object GameClientInputPacketFactory {

    val packetHandler = GameClientInputPacketHandler()

    init {
        /**
         * Main packets
         */
        addPacket(InAuthClient(State.CONNECTED)) // 5.1
        //addPacket(CM_L2AUTH_LOGIN_CHECK(0x015F, State.CONNECTED)) // 5.1

    }

    private fun addPacket(prototype: GameClientInputPacket) {
        packetHandler.addPacketPrototype(prototype)
    }

}