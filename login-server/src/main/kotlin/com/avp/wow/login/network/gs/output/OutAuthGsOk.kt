package com.avp.wow.login.network.gs.output

import com.avp.wow.login.network.gs.LoginGsConnection
import com.avp.wow.login.network.gs.LoginGsOutputPacket

class OutAuthGsOk : LoginGsOutputPacket() {

    init {
        opCode = OP_CODE
    }

    override fun writeImpl(con: LoginGsConnection) {
        writeD(con.sessionId) // session id
    }

    companion object {
        const val OP_CODE = 0x03
    }

}