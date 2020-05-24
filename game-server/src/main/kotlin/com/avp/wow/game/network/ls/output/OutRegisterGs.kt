package com.avp.wow.game.network.ls.output

import com.avp.wow.game.network.GameNioServer
import com.avp.wow.game.network.ls.GameLsConnection
import com.avp.wow.game.network.ls.GameLsOutputPacket
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
class OutRegisterGs : GameLsOutputPacket() {

    init {
        opCode = OP_CODE
    }

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