package com.avp.wow.game.network.client.output

import com.avp.wow.game.network.client.GameClientConnection
import com.avp.wow.game.network.client.GameClientOutputPacket

class OutClientLoginCheckResponse(
    private val result: Boolean,
    private val accountName: String
) : GameClientOutputPacket() {

    init {
        opCode = OP_CODE
    }

    override fun writeImpl(con: GameClientConnection) {
        writeD(con.sessionId) // session id
        writeC(if (result) 1 else 0)
        writeS(accountName)
    }

    companion object {
        const val OP_CODE = 5
    }

}