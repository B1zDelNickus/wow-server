package com.avp.wow.network.client.login.input

import com.avp.wow.network.client.login.LoginServerConnection
import com.avp.wow.network.client.login.LoginServerInputPacket
import com.avp.wow.network.client.login.output.OutAuthGuard
import io.ktor.util.KtorExperimentalAPI
import java.nio.ByteBuffer

@KtorExperimentalAPI
class InInitSession(
    buffer: ByteBuffer,
    server: LoginServerConnection
) : LoginServerInputPacket(
    opCode = OP_CODE,
    server = server,
    buffer = buffer
) {

    private var sessionId: Int = 0
    private var publicRsaKey: ByteArray? = null
    private var blowfishKey: ByteArray? = null

    override fun readImpl() {
        sessionId = readD()
        publicRsaKey = readB(128)
        blowfishKey = readB(16)

        println(sessionId)
        println(publicRsaKey)
        println(blowfishKey)
    }

    override suspend fun runImpl() {
        connection?.enableEncryption(blowfishKey!!)
        connection?.sessionId = sessionId

        connection?.sendPacket(OutAuthGuard())
    }

    companion object {
        const val OP_CODE = 0x01
    }

}