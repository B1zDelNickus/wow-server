package com.avp.wow.network.ktor.login.client.output

import com.avp.wow.model.gs.GameServer
import com.avp.wow.network.ktor.login.client.LoginClientOutputPacket
import com.avp.wow.network.ktor.login.client.LoginClientConnection
import com.avp.wow.network.ktor.login.client.SessionKey
import com.avp.wow.service.gs.GameServersConfig
import io.ktor.util.KtorExperimentalAPI
import javax.crypto.SecretKey

@KtorExperimentalAPI
class OutEnterGameServerOk(
    private val server: GameServer
) : LoginClientOutputPacket() {

    override fun writeImpl(con: LoginClientConnection) {

        writeD(con.sessionKey!!.playOk1)
        writeD(con.sessionKey!!.playOk2)
        writeQ(1) // server ID
        writeB(server.serverIp)
        writeD(server.serverPort)

    }

    companion object {
        const val OP_CODE = 0x09
    }

}