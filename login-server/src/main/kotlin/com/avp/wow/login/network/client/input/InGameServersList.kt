package com.avp.wow.login.network.client.input

import com.avp.wow.login.network.client.LoginClientConnection.Companion.State
import com.avp.wow.login.network.client.LoginClientInputPacket
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
class InGameServersList(vararg states: State) : LoginClientInputPacket(OP_CODE, states.toList()) {

    override fun readImpl() = Unit

    override suspend fun runImpl() {
        log.debug { "Requested Servers List" }
    }

    companion object {
        const val OP_CODE = 6
    }
}