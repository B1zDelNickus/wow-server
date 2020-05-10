package com.avp.wow.network.ktor.login.gs.input

import com.avp.wow.network.ktor.login.gs.LoginGsConnection
import com.avp.wow.network.ktor.login.gs.LoginGsConnection.Companion.State.AUTHED
import com.avp.wow.network.ktor.login.gs.LoginGsConnection.Companion.State.REGISTERED
import com.avp.wow.network.ktor.login.gs.LoginGsInputPacket
import com.avp.wow.network.ktor.login.gs.output.OutAuthGsOk
import com.avp.wow.network.ktor.login.gs.output.OutRegisterGsOk
import com.avp.wow.service.gs.GameServersConfig
import io.ktor.util.KtorExperimentalAPI
import java.nio.ByteBuffer

@KtorExperimentalAPI
class InRegisterGs(
    buffer: ByteBuffer,
    client: LoginGsConnection
) : LoginGsInputPacket(
    opCode = OP_CODE,
    client = client,
    buffer = buffer
) {

    private var sessionId = 0
    private var serverId = 0
    private var serverPort = 0
    private var serverName = ""

    override fun readImpl() {
        sessionId = readD()
        serverId = readC()
        serverPort = readH()
        serverName = readS()
    }

    override suspend fun runImpl() {
        connection?.let { con ->
            when (con.sessionId) {
                sessionId -> {
                    when {
                        GameServersConfig
                            .gameServersService
                            .registerGameServer(
                                id = serverId,
                                host = con.ip.split(":")[0],
                                port = serverPort,
                                name = serverName
                            ) -> {
                            con.state = REGISTERED
                            con.sendPacket(OutRegisterGsOk())
                        }
                        else -> {

                        }
                    }
                }
                else -> {

                }
            }
        }

    }

    companion object {
        const val OP_CODE = 0x04
    }
}