package com.avp.wow.game.network.client.input

import com.avp.wow.game.network.client.GameClientConnection.Companion.State
import com.avp.wow.game.network.client.GameClientInputPacket
import com.avp.wow.game.network.client.output.OutAuthClientOk
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
class InAuthClient(vararg states: State) : GameClientInputPacket(OP_CODE, states.toList()) {

    private var sessionId = 0

    override fun readImpl() {
        sessionId = readD()
    }

    override suspend fun runImpl() {
        connection?.let { con ->
            when (con.sessionId) {
                sessionId -> {
                    //con.state = State.AUTHED
                    con.sendPacket(OutAuthClientOk())
                }
                else -> {

                }
            }
        }
    }

    companion object {
        const val OP_CODE = 0x02
    }
}