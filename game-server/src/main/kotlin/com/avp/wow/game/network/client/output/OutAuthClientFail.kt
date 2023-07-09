package com.avp.wow.game.network.client.output

import com.avp.wow.game.network.client.GameClientConnection
import com.avp.wow.game.network.client.GameClientOutputPacket

class OutAuthClientFail(
    private val wrongSessionId: Int
) : GameClientOutputPacket() {

    init {
        opCode = OP_CODE
    }

    override fun writeImpl(con: GameClientConnection) {
        writeD(wrongSessionId) // session id
    }

    companion object {
        const val OP_CODE = 9
    }

}