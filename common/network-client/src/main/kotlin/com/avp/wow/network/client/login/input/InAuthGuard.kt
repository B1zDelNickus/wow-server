package com.avp.wow.network.client.login.input

import com.avp.wow.network.client.login.LoginServerConnection
import com.avp.wow.network.client.login.LoginServerConnection.Companion.State.AUTHED_GG
import com.avp.wow.network.client.login.LoginServerInputPacket
import io.ktor.util.KtorExperimentalAPI
import java.nio.ByteBuffer

@KtorExperimentalAPI
class InAuthGuard(
    buffer: ByteBuffer,
    server: LoginServerConnection
) : LoginServerInputPacket(
    opCode = OP_CODE,
    server = server,
    buffer = buffer
) {

    private var sessionId: Int = 0

    override fun readImpl() {
        sessionId = readD()
    }

    override suspend fun runImpl() {
        connection?.state = AUTHED_GG
        log.debug { "AUTHED GG" }
    }

    companion object {
        const val OP_CODE = 0x01
    }

}