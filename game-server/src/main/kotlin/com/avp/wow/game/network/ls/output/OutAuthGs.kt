package com.avp.wow.game.network.ls.output

import com.avp.wow.game.network.ls.GameLsConnection
import com.avp.wow.game.network.ls.GameLsOutputPacket

class OutAuthGs : GameLsOutputPacket() {

    init {
        opCode = OP_CODE
    }

    override fun writeImpl(con: GameLsConnection) {
        //println("########## $sessionId - ${con.sessionId}")
        writeD(con.sessionId) // session id TODO obfuscate with RSA public key
    }

    companion object {
        const val OP_CODE = 2
    }

}