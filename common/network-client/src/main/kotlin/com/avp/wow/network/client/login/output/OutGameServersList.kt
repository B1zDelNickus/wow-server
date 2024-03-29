package com.avp.wow.network.client.login.output

import com.avp.wow.network.client.login.LoginServerConnection
import com.avp.wow.network.client.login.LoginServerOutputPacket
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
class OutGameServersList : LoginServerOutputPacket() {

    init {
        opCode = OP_CODE
    }

    override fun writeImpl(con: LoginServerConnection) {
        writeD(con.sessionId) // session id
    }

    companion object {
        const val OP_CODE = 0x06
    }

}