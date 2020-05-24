package com.avp.wow.network.client.login.input

import com.avp.wow.network.client.login.LoginServerConnection.Companion.State
import com.avp.wow.network.client.login.LoginServerInputPacket
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
class InAuthClientOk(vararg states: State) : LoginServerInputPacket(OP_CODE, states.toList()) {

    private var sessionId: Int = 0

    override fun readImpl() {
        sessionId = readD()
    }

    override suspend fun runImpl() {
        connection?.let { con ->
            when (con.sessionId) {
                sessionId -> {
                    con.state = State.AUTHED_GG
                    log.debug { "AUTHED GG, SHOW LOGIN SCREEN" }
                }
                else -> {
                    log.error { "Session doesn't matches: ${con.sessionId} != $sessionId" }
                    // DISCONECT
                }
            }
        }

    }

    companion object {
        const val OP_CODE = 0x03
    }

}