package com.avp.wow.login.network.client.output

import com.avp.wow.login.network.client.LoginClientConnection
import com.avp.wow.login.network.client.LoginClientOutputPacket
import com.avp.wow.login.network.client.SessionKey
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
class OutLoginOk(
    private val sessionKey: SessionKey
) : LoginClientOutputPacket() {

    init {
        opCode = OP_CODE
    }

    override fun writeImpl(con: LoginClientConnection) {
        writeD(con.sessionId)
        writeQ(sessionKey.accountId)
        writeD(sessionKey.loginOk)
    }

    companion object {
        const val OP_CODE = 5
    }

}