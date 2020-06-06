package com.avp.wow.network.client.login.input

import com.avp.wow.network.client.factories.LoginServerOutputPacketFactory
import com.avp.wow.network.client.factories.LoginServerOutputPacketFactory.packetHandler
import com.avp.wow.network.client.login.LoginServerConnection.Companion.State
import com.avp.wow.network.client.login.LoginServerInputPacket
import com.avp.wow.network.client.login.output.OutAuthClient
import com.avp.wow.network.client.login.output.OutEnterGameServer
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
class InLoginOk(vararg states: State) : LoginServerInputPacket(OP_CODE, states.toList()) {

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
                    con.state = State.AUTHED_LOGIN
                    packetHandler.handle(OutEnterGameServer.OP_CODE)
                        ?.let { pck -> con.sendPacket(pck) }
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