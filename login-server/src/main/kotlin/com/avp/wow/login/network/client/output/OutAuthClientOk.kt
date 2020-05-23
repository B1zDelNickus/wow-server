package com.avp.wow.login.network.client.output

import com.avp.wow.login.network.client.LoginClientConnection
import com.avp.wow.login.network.client.LoginClientOutputPacket
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
class OutAuthClientOk : LoginClientOutputPacket() {

    override fun writeImpl(con: LoginClientConnection) {
        writeD(con.sessionId) // session id
    }

    companion object {
        const val OP_CODE = 3
    }

}