package com.avp.wow.network.client.game.output

import com.avp.wow.network.client.game.GameServerConnection
import com.avp.wow.network.client.game.GameServerOutputPacket
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
class OutAuthClient : GameServerOutputPacket() {

    init {
        opCode = OP_CODE
    }

    override fun writeImpl(con: GameServerConnection) {
        writeD(con.sessionId) // session id
    }

    companion object {
        const val OP_CODE = 2
    }

}