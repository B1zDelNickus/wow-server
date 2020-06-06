package com.avp.wow.login.network.factories

import com.avp.wow.login.network.client.LoginClientConnection.Companion.State
import com.avp.wow.login.network.client.LoginClientInputPacket
import com.avp.wow.login.network.client.LoginClientInputPacketHandler
import com.avp.wow.login.network.client.input.InAuthClient
import com.avp.wow.login.network.client.input.InEnterGameServer
import com.avp.wow.login.network.client.input.InGameServersList
import com.avp.wow.login.network.client.input.InLogin
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
object LoginClientInputPacketFactory {

    val packetHandler = LoginClientInputPacketHandler()

    init {
        /**
         * Main packets
         */
        addPacket(InAuthClient(State.CONNECTED))
        addPacket(InLogin(State.AUTHED_GG))
        addPacket(InGameServersList(State.AUTHED_LOGIN))
        addPacket(InEnterGameServer(State.AUTHED_LOGIN))
        //addPacket(CM_L2AUTH_LOGIN_CHECK(0x015F, State.CONNECTED)) // 5.1

    }

    private fun addPacket(prototype: LoginClientInputPacket) {
        packetHandler.addPacketPrototype(prototype)
    }

}