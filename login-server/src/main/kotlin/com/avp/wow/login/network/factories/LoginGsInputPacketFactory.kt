package com.avp.wow.login.network.factories

import com.avp.wow.login.network.gs.LoginGsConnection.Companion.State
import com.avp.wow.login.network.gs.LoginGsInputPacket
import com.avp.wow.login.network.gs.LoginGsInputPacketHandler
import com.avp.wow.login.network.gs.input.InAccountCheck
import com.avp.wow.login.network.gs.input.InAuthGs
import com.avp.wow.login.network.gs.input.InRegisterGs
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
object LoginGsInputPacketFactory {

    val packetHandler = LoginGsInputPacketHandler()

    init {
        /**
         * Main packets
         */
        addPacket(InAuthGs(State.CONNECTED))
        addPacket(InRegisterGs(State.AUTHED))
        addPacket(InAccountCheck(State.REGISTERED))
        //addPacket(CM_L2AUTH_LOGIN_CHECK(0x015F, State.CONNECTED)) // 5.1

    }

    private fun addPacket(prototype: LoginGsInputPacket) {
        packetHandler.addPacketPrototype(prototype)
    }

}