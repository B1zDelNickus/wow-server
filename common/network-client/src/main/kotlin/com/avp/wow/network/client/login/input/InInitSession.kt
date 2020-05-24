package com.avp.wow.network.client.login.input

import com.avp.wow.network.client.factories.LoginServerOutputPacketFactory.packetHandler
import com.avp.wow.network.client.login.LoginServerConnection.Companion.State
import com.avp.wow.network.client.login.LoginServerInputPacket
import com.avp.wow.network.client.login.output.OutAuthClient
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
class InInitSession(vararg states: State) : LoginServerInputPacket(OP_CODE, states.toList()) {

    private var sessionId: Int = 0
    private var publicRsaKey: ByteArray? = null
    private var blowfishKey: ByteArray? = null

    override fun readImpl() {
        sessionId = readD()
        publicRsaKey = readB(162) // (128)
        blowfishKey = readB(16)
    }

    override suspend fun runImpl() {
        connection?.let { con ->
            con.enableEncryption(blowfishKey!!)
            con.sessionId = sessionId
            con.publicRsa = publicRsaKey
            packetHandler.handle(OutAuthClient.OP_CODE)
                ?.let { pck -> con.sendPacket(pck) }
        }

    }

    companion object {
        const val OP_CODE = 0x01
    }

}