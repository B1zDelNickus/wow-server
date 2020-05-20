package com.avp.wow.login.network.client.input

import com.avp.wow.login.network.client.LoginClientConnection
import com.avp.wow.login.network.client.LoginClientInputPacket
import com.avp.wow.login.network.client.output.OutAuthClientOk
import io.ktor.util.KtorExperimentalAPI
import java.nio.ByteBuffer

@KtorExperimentalAPI
class InAuthClient(
    buffer: ByteBuffer,
    client: LoginClientConnection
) : LoginClientInputPacket(
    opCode = OP_CODE,
    client = client,
    buffer = buffer
) {

    private var sessionId: Int = 0

    override fun readImpl() {
        sessionId = readD()
    }

    override suspend fun runImpl() {
        when (connection?.sessionId) {
            sessionId -> {
                connection?.state = LoginClientConnection.Companion.State.AUTHED_GG
                connection?.sendPacket(
                    OutAuthClientOk()
                )
            }
            else -> {
                /**
                 * Session id is not ok - inform client that smth went wrong - dc client
                 */

                log.error { "Sessions doesnt match: ${connection?.sessionId} != $sessionId" }

                //con.close(SM_LOGIN_FAIL(AionAuthResponse.SYSTEM_ERROR), false)
            }
        }
    }

    companion object {
        const val OP_CODE = 0x02
    }
}