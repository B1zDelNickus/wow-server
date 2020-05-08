package com.avp.wow.network.ktor.login.client.input

import com.avp.wow.network.ktor.login.client.LoginClientConnection
import com.avp.wow.network.ktor.login.client.LoginClientInputPacket
import com.avp.wow.network.ktor.login.client.output.OutEnterGameServerOk
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
                    null == server || !server.isOnline -> {
                        // con.sendPacket(new SM_PLAY_FAIL(AionAuthResponse.SERVER_DOWN));
                    }
                    /* server.IsFull() */ // con.sendPacket(new SM_PLAY_FAIL(AionAuthResponse.SERVER_FULL));
                    else -> {
                        con.joinedGs = true
                        con.sendPacket(
                            OutEnterGameServerOk(
                                server = server
                            )
                        )
                    }
                }

            } else {
                // con.close(new SM_LOGIN_FAIL(AionAuthResponse.SYSTEM_ERROR), false);
            }
        }
    }

    companion object {
        const val OP_CODE = 0x08
    }
}