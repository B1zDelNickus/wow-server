package com.avp.wow.network.client.login.input

import com.avp.wow.network.client.login.LoginServerConnection
import com.avp.wow.network.client.login.LoginServerConnection.Companion.State.AUTHED_GG
import com.avp.wow.network.client.login.LoginServerInputPacket
import com.avp.wow.network.client.login.output.OutGameServersList
import io.ktor.util.KtorExperimentalAPI
import java.nio.ByteBuffer

@KtorExperimentalAPI
class InEnterGameServerOk(
    buffer: ByteBuffer,
    server: LoginServerConnection
) : LoginServerInputPacket(
    opCode = OP_CODE,
    server = server,
    buffer = buffer
) {

    private var playOk1: Int = 0
    private var playOk2: Int = 0
    private var serverId: Long = 0
    private var serverHostIp: String = ""
    private var serverHostPort: Int = 0

    override fun readImpl() {
        playOk1 = readD()
        playOk2 = readD()
        serverId = readQ()
        serverHostIp = readB(4).map { it.toString() }.joinToString(".")
        serverHostPort = readD()

    }

    override suspend fun runImpl() {
        connection?.let { con ->
            con.playOk1 = playOk1
            con.playOk2 = playOk2

            log.debug { "Connect to Game Server on host: $serverHostIp:$serverHostPort." }

            //con.nio.connect()
        }
    }

    companion object {
        const val OP_CODE = 0x09
    }

}