package com.avp.wow.network.ktor.login.gs.input

import com.avp.wow.network.ktor.login.gs.LoginGsConnection
import com.avp.wow.network.ktor.login.gs.LoginGsInputPacket
import io.ktor.util.KtorExperimentalAPI
import java.nio.ByteBuffer

@KtorExperimentalAPI
class InGsAuth(
    buffer: ByteBuffer,
    client: LoginGsConnection
) : LoginGsInputPacket(
    opCode = OP_CODE,
    client = client,
    buffer = buffer
) {

    override fun readImpl() {

    }

    override suspend fun runImpl() {
        log.debug { "RECEIVED GS AUTH PACKET!" }
    }

    companion object {
        const val OP_CODE = 0x01
    }
}