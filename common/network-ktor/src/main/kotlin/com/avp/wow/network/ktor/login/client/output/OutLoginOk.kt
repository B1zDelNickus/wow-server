package com.avp.wow.network.ktor.login.client.output

import com.avp.wow.network.ktor.login.client.LoginClientOutputPacket
import com.avp.wow.network.ktor.login.client.LoginClientConnection
import com.avp.wow.network.ktor.login.client.SessionKey
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
class OutLoginOk(
    private val sessionKey: SessionKey
) : LoginClientOutputPacket() {

    override fun writeImpl(con: LoginClientConnection) {
        writeQ(sessionKey.accountId)
        writeD(sessionKey.loginOk)
    }

    companion object {
        const val OP_CODE = 0x05
    }

}