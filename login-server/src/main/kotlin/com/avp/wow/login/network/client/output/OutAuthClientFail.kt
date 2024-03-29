package com.avp.wow.login.network.client.output

import com.avp.wow.login.network.client.LoginClientConnection
import com.avp.wow.login.network.client.LoginClientOutputPacket
import com.avp.wow.service.auth.enums.AuthResponse

class OutAuthClientFail(private val response: AuthResponse) : LoginClientOutputPacket() {

    init {
        opCode = OP_CODE
    }

    override fun writeImpl(con: LoginClientConnection) {
        writeD(con.sessionId) // session id
        writeD(response.code)
    }

    companion object {
        const val OP_CODE = 10
    }

}