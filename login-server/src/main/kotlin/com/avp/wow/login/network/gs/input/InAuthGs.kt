package com.avp.wow.login.network.gs.input

import com.avp.wow.login.network.gs.LoginGsConnection.Companion.State
import com.avp.wow.login.network.gs.LoginGsInputPacket
import com.avp.wow.login.network.gs.output.OutAuthGsFail
import com.avp.wow.login.network.gs.output.OutAuthGsOk
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
class InAuthGs(vararg states: State) : LoginGsInputPacket(OP_CODE, states.toList()) {

    private var sessionId = 0

    override fun readImpl() {
        sessionId = readD()
    }

    override suspend fun runImpl() {
        connection?.let { con ->
            when (con.sessionId) {
                sessionId -> {
                    con.state = State.AUTHED
                    sendPacket(OutAuthGsOk())
                }
                else -> {
                    /**
                     * Session id is not ok - inform client that smth went wrong - dc GS
                     */
                    log.error { "Sessions doesnt match: ${con.sessionId} != $sessionId" }
                    con.close(OutAuthGsFail(wrongSessionId = sessionId), true)
                }
            }
        }

    }

    companion object {
        const val OP_CODE = 2
    }
}