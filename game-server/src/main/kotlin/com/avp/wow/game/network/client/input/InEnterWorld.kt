package com.avp.wow.game.network.client.input

import com.avp.wow.game.network.client.GameClientConnection.Companion.State
import com.avp.wow.game.network.client.GameClientInputPacket
import com.avp.wow.game.network.client.output.OutEnterWorldOk
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
class InEnterWorld(vararg states: State) : GameClientInputPacket(OP_CODE, states.toList()) {

    private var sessionId = 0

    override fun readImpl() {
        sessionId = readD()
    }

    override suspend fun runImpl() {
        connection?.let { con ->
            when (con.sessionId) {
                sessionId -> {
                    con.state = State.IN_GAME
                    con.sendPacket(OutEnterWorldOk())
                }
                else -> {

                }
            }
        }
    }

    companion object {
        const val OP_CODE = 0x06
    }
}