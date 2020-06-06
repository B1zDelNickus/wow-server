package com.avp.wow.login.network.client.output

import com.avp.wow.login.network.client.LoginClientConnection
import com.avp.wow.login.network.client.LoginClientOutputPacket
import com.avp.wow.model.gs.GameServer
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
class OutEnterGameServerOk(
    private val server: GameServer
) : LoginClientOutputPacket() {

    init {
        opCode = OP_CODE
    }

    override fun writeImpl(con: LoginClientConnection) {
        writeD(con.sessionId)
        writeD(con.sessionKey!!.playOk1)
        writeD(con.sessionKey!!.playOk2)
        writeC(server.id) // server ID
        writeS(server.host)
        writeH(server.port)
    }

    companion object {
        const val OP_CODE = 9
    }

}