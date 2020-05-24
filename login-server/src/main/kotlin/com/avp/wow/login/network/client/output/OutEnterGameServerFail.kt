package com.avp.wow.login.network.client.output

import com.avp.wow.login.network.client.LoginClientConnection
import com.avp.wow.login.network.client.LoginClientOutputPacket
import com.avp.wow.model.gs.GameServer
import com.avp.wow.service.auth.enums.AuthResponse
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
class OutEnterGameServerFail(
    private val response: AuthResponse
) : LoginClientOutputPacket() {

    init {
        opCode = OP_CODE
    }

    override fun writeImpl(con: LoginClientConnection) {
        writeD(con.sessionId)
        writeD(response.code)
    }

    companion object {
        const val OP_CODE = 12
    }

}