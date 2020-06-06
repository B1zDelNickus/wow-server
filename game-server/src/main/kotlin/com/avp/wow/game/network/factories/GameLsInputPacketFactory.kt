package com.avp.wow.game.network.factories

import com.avp.wow.game.network.ls.GameLsConnection.Companion.State
import com.avp.wow.game.network.ls.GameLsInputPacket
import com.avp.wow.game.network.ls.GameLsInputPacketHandler
import com.avp.wow.game.network.ls.input.InAccountCheckResponse
import com.avp.wow.game.network.ls.input.InAuthGsOk
import com.avp.wow.game.network.ls.input.InInitSession
import com.avp.wow.game.network.ls.input.InRegisterGsOk
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
object GameLsInputPacketFactory {

    val packetHandler = GameLsInputPacketHandler()

    init {
        /**
         * Main packets
         */
        addPacket(InInitSession(State.CONNECTED))
        addPacket(InAuthGsOk(State.CONNECTED))
        addPacket(InRegisterGsOk(State.AUTHED))
        addPacket(InAccountCheckResponse(State.REGISTERED))
        //addPacket(CM_L2AUTH_LOGIN_CHECK(0x015F, State.CONNECTED)) // 5.1

    }

    private fun addPacket(prototype: GameLsInputPacket) {
        packetHandler.addPacketPrototype(prototype)
    }

}