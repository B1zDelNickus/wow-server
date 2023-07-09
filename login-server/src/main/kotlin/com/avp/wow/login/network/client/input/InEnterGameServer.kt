package com.avp.wow.login.network.client.input

import com.avp.wow.login.network.client.LoginClientConnection.Companion.State
import com.avp.wow.login.network.client.LoginClientInputPacket
import com.avp.wow.login.network.client.output.OutAuthClientFail
import com.avp.wow.login.network.client.output.OutEnterGameServerFail
import com.avp.wow.login.network.client.output.OutEnterGameServerOk
import com.avp.wow.login.network.factories.LoginClientOutputPacketFactory.packetHandler
import com.avp.wow.service.auth.enums.AuthResponse
import com.avp.wow.service.gs.GameServersConfig

class InEnterGameServer(vararg states: State) : LoginClientInputPacket(OP_CODE, states.toList()) {

    private var accountId = 0L
    private var loginOk = 0

    override fun readImpl() {
        accountId = readQ()
        loginOk = readD()
    }

    override suspend fun runImpl() {
        connection?.let { con ->
            if (con.sessionKey!!.checkLogin(accountId = accountId, loginOk = loginOk)) {

                val server = GameServersConfig
                    .gameServersService
                    .gameServers[con.account!!.currentServerId]

                when {
                    null == server || !server.isOnline -> packetHandler.handle(
                        OutEnterGameServerFail.OP_CODE,
                        AuthResponse.SERVER_DOWN
                    )
                        ?.let { pck -> sendPacket(pck) }
                    server.isFull -> packetHandler.handle(OutEnterGameServerFail.OP_CODE, AuthResponse.SERVER_FULL)
                        ?.let { pck -> sendPacket(pck) }
                    else -> {
                        con.joinedGs = true
                        packetHandler.handle(OutEnterGameServerOk.OP_CODE, server)
                            ?.let { pck -> sendPacket(pck) }
                    }
                }

            } else {
                log.error { "Sessions keys doesn't match." }
                packetHandler.handle(OutAuthClientFail.OP_CODE, AuthResponse.SYSTEM_ERROR)
                    ?.let { pck -> con.close(pck, false) }
            }
        }
    }

    companion object {
        const val OP_CODE = 8
    }
}