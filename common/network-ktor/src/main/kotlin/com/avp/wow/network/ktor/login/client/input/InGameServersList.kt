package com.avp.wow.network.ktor.login.client.input

import com.avp.wow.network.ktor.login.client.LoginClientConnection
import com.avp.wow.network.ktor.login.client.LoginClientConnection.Companion.State.AUTHED_GG
import com.avp.wow.network.ktor.login.client.LoginClientInputPacket
import com.avp.wow.network.ktor.login.client.output.OutAuthGuard
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
        const val OP_CODE = 0x06
    }
}