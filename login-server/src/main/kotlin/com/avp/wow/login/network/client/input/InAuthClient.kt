package com.avp.wow.login.network.client.input

import com.avp.wow.login.network.client.LoginClientConnection.Companion.State
import com.avp.wow.login.network.client.LoginClientInputPacket
import com.avp.wow.login.network.client.output.OutAuthClientFail
import com.avp.wow.login.network.client.output.OutAuthClientOk
import com.avp.wow.service.auth.enums.AuthResponse
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
class InAuthClient(vararg states: State) : LoginClientInputPacket(OP_CODE, states.toList()) {

    private var sessionId: Int = 0

    override fun readImpl() {
        sessionId = readD()
    }

    override suspend fun runImpl() {
        connection?.let { con ->
            when (con.sessionId) {
                sessionId -> {
                    con.state = State.AUTHED_GG
                    con.sendPacket(
                        OutAuthClientOk()
                    )
                }
                else -> {
                    /**
                     * Session id is not ok - inform client that smth went wrong - dc client
                     */
                    log.error { "Sessions doesnt match: ${con.sessionId} != $sessionId" }
                    con.close(OutAuthClientFail(AuthResponse.SYSTEM_ERROR), false)
                }
            }
        }

    }

    companion object {
        const val OP_CODE = 2
    }
}