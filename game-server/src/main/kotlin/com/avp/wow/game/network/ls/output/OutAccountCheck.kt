package com.avp.wow.game.network.ls.output

import com.avp.wow.game.network.ls.GameLsConnection
import com.avp.wow.game.network.ls.GameLsOutputPacket
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
class OutAccountCheck(
    private val accountId: Long,
    private val loginOk: Int,
    private val playOk1: Int,
    private val playOk2: Int
) : GameLsOutputPacket() {

    override fun writeImpl(con: GameLsConnection) {
        writeD(con.sessionId) // session id TODO obfuscate with RSA public key
        writeQ(accountId)
        writeD(loginOk)
        writeD(playOk1)
        writeD(playOk2)
    }

    companion object {
        const val OP_CODE = 0x05
    }

}