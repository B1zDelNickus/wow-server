package com.avp.wow.network.ktor.login.gs.input

import com.avp.wow.network.ktor.login.gs.LoginGsConnection
import com.avp.wow.network.ktor.login.gs.LoginGsConnection.Companion.State.AUTHED
import com.avp.wow.network.ktor.login.gs.LoginGsInputPacket
import com.avp.wow.network.ktor.login.gs.output.OutAuthGsOk
import io.ktor.util.KtorExperimentalAPI
import java.nio.ByteBuffer

@KtorExperimentalAPI
class InAuthGs(
    buffer: ByteBuffer,
    client: LoginGsConnection
) : LoginGsInputPacket(
    opCode = OP_CODE,
    client = client,
    buffer = buffer
) {

    private var sessionId = 0

    override fun readImpl() {
        sessionId = readD()
    }

    override suspend fun runImpl() {
        connection?.let { con ->
            when (con.sessionId) {
                sessionId -> {
                    con.state = AUTHED
                    sendPacket(OutAuthGsOk())
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