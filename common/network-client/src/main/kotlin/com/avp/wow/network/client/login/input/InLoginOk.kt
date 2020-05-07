package com.avp.wow.network.client.login.input

import com.avp.wow.network.client.login.LoginServerConnection
import com.avp.wow.network.client.login.LoginServerConnection.Companion.State.AUTHED_GG
import com.avp.wow.network.client.login.LoginServerInputPacket
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

    override fun readImpl() {
        accountId = readQ()
        loginOk = readD()
    }

    override suspend fun runImpl() {
        connection?.let { con ->
            con.sendPacket(OutGameServersList())
        }
    }

    companion object {
        const val OP_CODE = 0x05
    }

}