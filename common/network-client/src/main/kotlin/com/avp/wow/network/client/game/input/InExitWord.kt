package com.avp.wow.network.client.game.input

import com.avp.wow.network.client.game.GameServerConnection.Companion.State
import com.avp.wow.network.client.game.GameServerInputPacket
import com.avp.wow.network.client.game.output.OutClientLoginCheck
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
class InExitWord(vararg states: State) : GameServerInputPacket(OP_CODE, states.toList()) {

    private var sessionId: Int = 0

    override fun readImpl() {
        sessionId = readD()
    }

    override suspend fun runImpl() {
        connection?.let { con ->
            when (con.sessionId) {
                sessionId -> {
                    con.state = State.AUTHED // TODO or else??
                    log.debug { "Exit to Login Screen" }
                }
                else -> {

                }
            }
        }
    }

    companion object {
        const val OP_CODE = 0x08
    }
}