package com.avp.wow.login.network.client.input

import com.avp.wow.login.network.client.LoginClientConnection
import com.avp.wow.login.network.client.LoginClientInputPacket
import com.avp.wow.login.network.client.output.OutAuthClientFail
import com.avp.wow.login.network.client.output.OutEnterGameServerFail
import com.avp.wow.login.network.client.output.OutEnterGameServerOk
import com.avp.wow.service.auth.enums.AuthResponse
import com.avp.wow.service.gs.GameServersConfig
import io.ktor.util.KtorExperimentalAPI
import java.nio.ByteBuffer

@KtorExperimentalAPI
class InEnterGameServer(
    buffer: ByteBuffer,
    client: LoginClientConnection
) : LoginClientInputPacket(
    opCode = OP_CODE,
    client = client,
    buffer = buffer
) {

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
                    null == server || !server.isOnline -> con.sendPacket(OutEnterGameServerFail(AuthResponse.SERVER_DOWN))
                    server.isFull -> con.sendPacket(OutEnterGameServerFail(AuthResponse.SERVER_FULL))
                    else -> {
                        con.joinedGs = true
                        con.sendPacket(OutEnterGameServerOk(server = server))
                    }
                }

            } else {
                log.error { "Sessions keys doesn't match." }
                con.close(OutAuthClientFail(AuthResponse.SYSTEM_ERROR), false)
            }
        }
    }

    companion object {
        const val OP_CODE = 8
    }
}