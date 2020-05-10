package com.avp.wow.network.ktor.login.gs.output

import com.avp.wow.network.ktor.login.gs.LoginGsConnection
import com.avp.wow.network.ktor.login.gs.LoginGsOutputPacket
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
class OutRegisterGsOk : LoginGsOutputPacket() {

    override fun writeImpl(con: LoginGsConnection) {
        writeD(con.sessionId) // session id
    }

    companion object {
        const val OP_CODE = 0x05
    }

}