package com.avp.wow.network.ktor.game.ls.output

import com.avp.wow.network.ktor.game.GameNioServer
import com.avp.wow.network.ktor.game.ls.GameLsConnection
import com.avp.wow.network.ktor.game.ls.GameLsOutputPacket
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