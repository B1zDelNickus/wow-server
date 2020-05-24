package com.avp.wow.login.network.gs.input

import com.avp.wow.login.network.factories.LoginGsOutputPacketFactory
import com.avp.wow.login.network.factories.LoginGsOutputPacketFactory.packetHandler
import com.avp.wow.login.network.gs.LoginGsConnection.Companion.State
import com.avp.wow.login.network.gs.LoginGsInputPacket
import com.avp.wow.login.network.gs.output.OutAuthGsFail
import com.avp.wow.login.network.gs.output.OutRegisterGsFail
import com.avp.wow.login.network.gs.output.OutRegisterGsOk
import com.avp.wow.service.gs.GameServersConfig
import com.avp.wow.service.gs.enums.GsRegisterResponse
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
class InRegisterGs(vararg states: State) : LoginGsInputPacket(OP_CODE, states.toList()) {

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

                    val response = GameServersConfig
                        .gameServersService
                        .registerGameServer(
                            id = serverId,
                            host = con.ip.split(":")[0],
                            port = serverPort,
                            name = serverName
                        )

                    when (response) {
                        GsRegisterResponse.REGISTERED -> {
                            con.state = State.REGISTERED
                            con.gameServerInfo = GameServersConfig.gameServersService.gameServers[serverId]!!
                            packetHandler.handle(OutRegisterGsOk.OP_CODE)
                                ?.let { pck -> sendPacket(pck) }
                        }
                        else -> {
                            // TODO in future multiple instances or load balancing
                            log.error { "GS already registered!!!" }
                            packetHandler.handle(OutRegisterGsFail.OP_CODE, response)
                                ?.let { pck -> con.close(pck, true) }
                        }
                    }
                }
                else -> {
                    log.error { "Sessions doesnt match: ${con.sessionId} != $sessionId" }
                    packetHandler.handle(OutAuthGsFail.OP_CODE, sessionId)
                        ?.let { pck -> con.close(pck, true) }
                }
            }
        }

    }

    companion object {
        const val OP_CODE = 4
    }
}