package com.avp.wow.login.network.gs.output

import com.avp.wow.login.network.gs.LoginGsConnection
import com.avp.wow.login.network.gs.LoginGsOutputPacket
import com.avp.wow.service.gs.enums.GsRegisterResponse

class OutRegisterGsFail(
    private val response: GsRegisterResponse
) : LoginGsOutputPacket() {

    init {
        opCode = OP_CODE
    }

    override fun writeImpl(con: LoginGsConnection) {
        writeD(con.sessionId) // session id
        writeD(response.code)
    }

    companion object {
        const val OP_CODE = 0x10
    }

}