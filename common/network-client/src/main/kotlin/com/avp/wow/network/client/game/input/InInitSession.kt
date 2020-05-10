package com.avp.wow.network.client.game.input

import com.avp.wow.network.client.game.GameServerConnection.Companion.State
import com.avp.wow.network.client.game.GameServerInputPacket
import com.avp.wow.network.client.game.output.OutAuthClient
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
class InInitSession(vararg states: State) : GameServerInputPacket(OP_CODE, states.toList()) {

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
            con.sendPacket(OutAuthClient())
        }
    }

    companion object {
        const val OP_CODE = 0x01
    }
}