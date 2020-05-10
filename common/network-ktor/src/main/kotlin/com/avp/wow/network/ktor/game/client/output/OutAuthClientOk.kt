package com.avp.wow.network.ktor.game.client.output

import com.avp.wow.network.ktor.game.client.GameClientConnection
import com.avp.wow.network.ktor.game.client.GameClientOutputPacket
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
class OutAuthClientOk : GameClientOutputPacket() {

    override fun writeImpl(con: GameClientConnection) {
        writeD(con.sessionId) // session id
    }

    companion object {
        const val OP_CODE = 0x03
    }

}