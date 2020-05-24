package com.avp.wow.game.network.ls.input

import com.avp.wow.game.network.ls.GameLsConnection.Companion.State
import com.avp.wow.game.network.ls.GameLsInputPacket
import com.avp.wow.game.network.ls.output.OutAuthGs
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
class InInitSession(
    vararg states: State
) : GameLsInputPacket(opCode = OP_CODE, states = states.toList()) {

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
            con.sendPacket(OutAuthGs())
        }
    }

    companion object {
        const val OP_CODE = 1
    }

}