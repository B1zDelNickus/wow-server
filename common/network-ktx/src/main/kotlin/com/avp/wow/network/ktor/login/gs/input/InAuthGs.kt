package com.avp.wow.network.ktor.login.gs.input

import com.avp.wow.network.ktor.login.gs.LoginGsConnection
import com.avp.wow.network.ktor.login.gs.LoginGsInputPacket
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
        log.debug { "RECEIVED GS AUTH PACKET!" }
    }

    companion object {
        const val OP_CODE = 0x02
    }
}