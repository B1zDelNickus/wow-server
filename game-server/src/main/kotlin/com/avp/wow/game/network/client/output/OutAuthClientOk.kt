package com.avp.wow.game.network.client.output

import com.avp.wow.game.network.client.GameClientConnection
import com.avp.wow.game.network.client.GameClientOutputPacket

class OutAuthClientOk : GameClientOutputPacket() {

    init {
        opCode = OP_CODE
    }

    override fun writeImpl(con: GameClientConnection) {
        writeD(con.sessionId) // session id
    }

    companion object {
        const val OP_CODE = 3
    }

}