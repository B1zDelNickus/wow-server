package com.avp.wow.game.network.ls.input

import com.avp.wow.game.network.ls.GameLsConnection.Companion.State
import com.avp.wow.game.network.ls.GameLsInputPacket

class InRegisterGsOk(
    vararg states: State
) : GameLsInputPacket(opCode = OP_CODE, states = states.toList()) {

    private var sessionId: Int = 0

    override fun readImpl() {
        sessionId = readD()
    }

    override suspend fun runImpl() {
        connection?.let { con ->
            when (con.sessionId) {
                sessionId -> {
                    con.state = State.REGISTERED
                    log.debug { "Successfully registered on LS" }
                }
                else -> {

                }
            }
        }
    }

    companion object {
        const val OP_CODE = 0x05
    }

}