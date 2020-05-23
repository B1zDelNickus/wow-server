package com.avp.wow.login.network.client.input

import com.avp.wow.login.network.client.LoginClientConnection
import com.avp.wow.login.network.client.LoginClientInputPacket
import io.ktor.util.KtorExperimentalAPI
import java.nio.ByteBuffer

@KtorExperimentalAPI
class InGameServersList(
    buffer: ByteBuffer,
    client: LoginClientConnection
) : LoginClientInputPacket(
    opCode = OP_CODE,
    client = client,
    buffer = buffer
) {

    override fun readImpl() = Unit

    override suspend fun runImpl() {
        log.debug { "Requested Servers List" }
    }

    companion object {
        const val OP_CODE = 6
    }
}