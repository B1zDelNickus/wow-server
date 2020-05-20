package com.avp.wow.game.network.client.output

import com.avp.wow.game.network.client.GameClientConnection
import com.avp.wow.game.network.client.GameClientOutputPacket
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
class OutClientLoginCheckResponse(
    private val result: Boolean,
    private val accountName: String
) : GameClientOutputPacket() {

    override fun writeImpl(con: GameClientConnection) {
        writeD(con.sessionId) // session id
        writeC(if (result) 1 else 0)
        writeS(accountName)
    }

    companion object {
        const val OP_CODE = 0x05
    }

}