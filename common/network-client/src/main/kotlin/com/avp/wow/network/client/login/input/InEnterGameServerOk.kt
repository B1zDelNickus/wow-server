package com.avp.wow.network.client.login.input

import com.avp.wow.network.KtorConnectionConfig
import com.avp.wow.network.client.KtorNioClient
import com.avp.wow.network.client.game.GameServerConnectionFactory
import com.avp.wow.network.client.game.SessionKey
import com.avp.wow.network.client.login.LoginServerConnection
import com.avp.wow.network.client.login.LoginServerInputPacket
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

    private var sessionId: Int = 0
    private var playOk1: Int = 0
    private var playOk2: Int = 0
    private var serverId: Int = 0
    private var serverHostIp: String = ""
    private var serverHostPort: Int = 0

    override fun readImpl() {
        sessionId = readD()
        playOk1 = readD()
        playOk2 = readD()
        serverId = readC()
        serverHostIp = readS()
        serverHostPort = readH()

    }

    override suspend fun runImpl() {
        connection?.let { con ->
            when (con.sessionId) {
                sessionId -> {
                    con.playOk1 = playOk1
                    con.playOk2 = playOk2

                    log.debug { "Connect to Game Server on host: $serverHostIp:$serverHostPort." }

                    (con.nio as KtorNioClient).sessionKey = SessionKey(
                        accountId = con.accountId,
                        loginOk = con.loginOk,
                        playOk1 = playOk1,
                        playOk2 = playOk2
                    )

                    con.nio.connectGameServer(
                        gameServerConfig = KtorConnectionConfig(
                            hostName = serverHostIp,
                            port = serverHostPort,
                            connectionName = "[${con}] GS Connection",
                            factory = GameServerConnectionFactory()
                        )
                    )
                }
                else -> {
                    log.error { "Session doesn't matches: ${con.sessionId} != $sessionId" }
                    // DISCONECT
                }
            }

        }
    }

    companion object {
        const val OP_CODE = 0x09
    }

}