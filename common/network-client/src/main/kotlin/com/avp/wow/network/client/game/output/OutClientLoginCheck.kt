package com.avp.wow.network.client.game.output

import com.avp.wow.network.client.KtorNioClient
import com.avp.wow.network.client.game.GameServerConnection
import com.avp.wow.network.client.game.GameServerOutputPacket
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
class OutClientLoginCheck : GameServerOutputPacket() {

    init {
        opCode = OP_CODE
    }

    override fun writeImpl(con: GameServerConnection) {
        writeD(con.sessionId) // session id
        val sessionKey = (con.nio as KtorNioClient).sessionKey!!
        writeD(sessionKey.playOk2)
        writeD(sessionKey.playOk1)
        writeQ(sessionKey.accountId)
        writeD(sessionKey.loginOk)
    }

    companion object {
        const val OP_CODE = 4
    }

}