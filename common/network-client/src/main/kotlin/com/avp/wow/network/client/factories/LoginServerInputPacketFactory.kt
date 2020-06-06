package com.avp.wow.network.client.factories

import com.avp.wow.network.client.login.LoginServerConnection.Companion.State
import com.avp.wow.network.client.login.LoginServerInputPacket
import com.avp.wow.network.client.login.LoginServerInputPacketHandler
import com.avp.wow.network.client.login.input.InAuthClientOk
import com.avp.wow.network.client.login.input.InEnterGameServerOk
import com.avp.wow.network.client.login.input.InInitSession
import com.avp.wow.network.client.login.input.InLoginOk
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
object LoginServerInputPacketFactory {

    val packetHandler = LoginServerInputPacketHandler()

    init {
        /**
         * Main packets
         */
        addPacket(InInitSession(State.CONNECTED)) // 5.1
        addPacket(InAuthClientOk(State.CONNECTED)) // 5.1
        addPacket(InLoginOk(State.AUTHED_GG)) // 5.1
        addPacket(InEnterGameServerOk(State.AUTHED_LOGIN)) // 5.1
        //addPacket(CM_L2AUTH_LOGIN_CHECK(0x015F, State.CONNECTED)) // 5.1

    }

    private fun addPacket(prototype: LoginServerInputPacket) {
        packetHandler.addPacketPrototype(prototype)
    }

}