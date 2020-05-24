package com.avp.wow.network.client.login.input

import com.avp.wow.network.client.login.LoginServerConnection
import com.avp.wow.network.client.login.LoginServerConnection.Companion.State.AUTHED_GG
import com.avp.wow.network.client.login.LoginServerConnection.Companion.State.AUTHED_LOGIN
import com.avp.wow.network.client.login.LoginServerInputPacket
import com.avp.wow.network.client.login.output.OutEnterGameServer
import com.avp.wow.network.client.login.output.OutGameServersList
import io.ktor.util.KtorExperimentalAPI
import java.nio.ByteBuffer

@KtorExperimentalAPI
class InLoginOk(
    buffer: ByteBuffer,
    server: LoginServerConnection
) : LoginServerInputPacket(
    opCode = OP_CODE,
    server = server,
    buffer = buffer
) {

    private var accountId: Long = 0
    private var loginOk: Int = 0
    private var sessionId: Int = 0

    override fun readImpl() {
        sessionId = readD()
        accountId = readQ()
        loginOk = readD()
    }

    override suspend fun runImpl() {
        connection?.let { con ->
            when (con.sessionId) {
                sessionId -> {
                    con.accountId = accountId
                    con.loginOk = loginOk
                    con.state = AUTHED_LOGIN
                    con.sendPacket(OutEnterGameServer())
                }
                else -> {
                    log.error { "Session doesn't matches: ${con.sessionId} != $sessionId" }
                    // DISCONECT
                }
            }
        }
    }

    companion object {
        const val OP_CODE = 0x05
    }

}