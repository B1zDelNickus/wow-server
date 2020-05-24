package com.avp.wow.game.network.ls.output

import com.avp.wow.game.network.ls.GameLsConnection
import com.avp.wow.game.network.ls.GameLsOutputPacket
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
class OutAuthGs(
    private val sessionId: Int
) : GameLsOutputPacket() {

    override fun writeImpl(con: GameLsConnection) {
        writeD(sessionId) // session id TODO obfuscate with RSA public key
    }

    companion object {
        const val OP_CODE = 2
    }

}