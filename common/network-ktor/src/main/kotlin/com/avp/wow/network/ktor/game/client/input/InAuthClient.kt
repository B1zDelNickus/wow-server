package com.avp.wow.network.ktor.game.client.input

import com.avp.wow.network.ktor.game.client.GameClientConnection.Companion.State
import com.avp.wow.network.ktor.game.client.GameClientInputPacket
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
class InAuthClient(vararg states: State) : GameClientInputPacket(OP_CODE, states.toList()) {

    private var sessionId = 0

    override fun readImpl() {
        sessionId = readD()
    }

    override suspend fun runImpl() {
        log.debug { "RECEIVED GS AUTH PACKET!" }
    }

    companion object {
        const val OP_CODE = 0x02
    }
}