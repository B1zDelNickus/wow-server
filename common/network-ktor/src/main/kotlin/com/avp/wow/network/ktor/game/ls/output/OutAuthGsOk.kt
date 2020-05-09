package com.avp.wow.network.ktor.game.ls.output

import com.avp.wow.network.ktor.game.ls.GameLsConnection
import com.avp.wow.network.ktor.game.ls.GameLsOutputPacket
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
class OutAuthGsOk : GameLsOutputPacket() {

    override fun writeImpl(con: GameLsConnection) {
        writeD(con.sessionId) // session id TODO obfuscate with RSA public key
    }

    companion object {
        const val OP_CODE = 0x02
    }

}