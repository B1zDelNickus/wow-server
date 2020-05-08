package com.avp.wow.network.client.factories

import com.avp.wow.network.client.game.GameServerInputPacket
import com.avp.wow.network.client.game.GameServerInputPacketHandler
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
object GameServerInputPacketFactory {

    val packetHandler = GameServerInputPacketHandler()

    init {
        /**
         * Main packets
         */
        //addPacket(CM_L2AUTH_LOGIN_CHECK(0x015F, State.CONNECTED)) // 5.1

    }

    private fun addPacket(prototype: GameServerInputPacket) {
        packetHandler.addPacketPrototype(prototype)
    }

}