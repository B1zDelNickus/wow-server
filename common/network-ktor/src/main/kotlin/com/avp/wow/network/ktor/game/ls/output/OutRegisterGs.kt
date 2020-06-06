package com.avp.wow.network.ktor.game.ls.output

import com.avp.wow.network.ktor.game.GameNioServer
import com.avp.wow.network.ktor.game.ls.GameLsConnection
import com.avp.wow.network.ktor.game.ls.GameLsOutputPacket
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
class OutRegisterGs : GameLsOutputPacket() {

    override fun writeImpl(con: GameLsConnection) {
        writeD(con.sessionId) // session id TODO obfuscate with RSA public key
        writeC(1) // Server ID from ENV
        writeH((con.nio as GameNioServer).clientPort)
        writeS("Base Server") // server name from ENV
    }

    companion object {
        const val OP_CODE = 0x04
    }

}