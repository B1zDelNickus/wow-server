package com.avp.wow.game.network.client.output

import com.avp.wow.game.network.client.GameClientConnection
import com.avp.wow.game.network.client.GameClientOutputPacket
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
class OutAuthClientFail(
    private val wrongSessionId: Int
) : GameClientOutputPacket() {

    override fun writeImpl(con: GameClientConnection) {
        writeD(wrongSessionId) // session id
    }

    companion object {
        const val OP_CODE = 9
    }

}