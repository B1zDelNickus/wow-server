package com.avp.wow.network.ktor.login.client.output

import com.avp.wow.network.ktor.login.client.LoginClientOutputPacket
import com.avp.wow.network.ktor.login.client.LoginClientConnection
import io.ktor.util.KtorExperimentalAPI
import javax.crypto.SecretKey

@KtorExperimentalAPI
class OutAuthClientOk : LoginClientOutputPacket() {

    override fun writeImpl(con: LoginClientConnection) {
        writeD(con.sessionId) // session id
    }

    companion object {
        const val OP_CODE = 0x03
    }

}