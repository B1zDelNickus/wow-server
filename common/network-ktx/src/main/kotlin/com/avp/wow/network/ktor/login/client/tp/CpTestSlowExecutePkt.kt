package com.avp.wow.network.ktor.login.client.tp

import com.avp.wow.network.ktor.login.client.LoginClientInputPacket
import com.avp.wow.network.ktor.login.client.LoginClientConnection
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.delay
import java.nio.ByteBuffer

@KtorExperimentalAPI
class CpTestSlowExecutePkt(
    buffer: ByteBuffer,
    client: LoginClientConnection
) : LoginClientInputPacket(
    opCode = OP_CODE,
    client = client,
    buffer = buffer
) {

    override fun readImpl() {
        log.info { "Read packet data" }
    }

    override suspend fun runImpl() {
        delay(10_000)
        log.info { "Run business logic" }
    }

    companion object {
        const val OP_CODE = 0x98
    }
}