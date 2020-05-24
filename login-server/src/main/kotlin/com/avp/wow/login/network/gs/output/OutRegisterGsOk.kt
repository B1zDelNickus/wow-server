package com.avp.wow.login.network.gs.output

import com.avp.wow.login.network.gs.LoginGsConnection
import com.avp.wow.login.network.gs.LoginGsOutputPacket
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
class OutRegisterGsOk : LoginGsOutputPacket() {

    init {
        opCode = OP_CODE
    }

    override fun writeImpl(con: LoginGsConnection) {
        writeD(con.sessionId) // session id
    }

    companion object {
        const val OP_CODE = 0x05
    }

}