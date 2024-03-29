package com.avp.wow.network.ktor.login.gs.output

import com.avp.wow.network.ktor.login.gs.LoginGsConnection
import com.avp.wow.network.ktor.login.gs.LoginGsOutputPacket
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
class OutAccountCheckResponse(
    private val accountId: Long,
    private val result: Boolean,
    private val accountName: String?,
    private val accessLevel: Byte
) : LoginGsOutputPacket() {

    override fun writeImpl(con: LoginGsConnection) {
        writeD(con.sessionId) // session id
        writeQ(accountId)
        writeC(if (result) 1 else 0)
        if (result) {
            writeS(accountName)
            writeC(accessLevel)
        }
    }

    companion object {
        const val OP_CODE = 0x07
    }

}