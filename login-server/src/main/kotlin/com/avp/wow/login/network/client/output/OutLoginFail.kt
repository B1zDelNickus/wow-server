package com.avp.wow.login.network.client.output

import com.avp.wow.login.network.client.LoginClientConnection
import com.avp.wow.login.network.client.LoginClientOutputPacket
import com.avp.wow.login.network.client.SessionKey
import com.avp.wow.service.auth.enums.AuthResponse
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
class OutLoginFail(
    private val response: AuthResponse
) : LoginClientOutputPacket() {

    override fun writeImpl(con: LoginClientConnection) {
        writeD(con.sessionId)
        writeD(response.code)
    }

    companion object {
        const val OP_CODE = 11
    }

}