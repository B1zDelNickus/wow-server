package com.avp.wow.game.network.client.input

import com.avp.wow.game.network.client.GameClientConnection.Companion.State
import com.avp.wow.game.network.client.GameClientInputPacket
import com.avp.wow.game.network.client.output.OutAuthClientFail
import com.avp.wow.game.network.client.output.OutEnterWorldOk
import com.avp.wow.game.network.factories.GameClientOutputPacketFactory.packetHandler

class InEnterWorld(vararg states: State) : GameClientInputPacket(OP_CODE, states.toList()) {

    private var sessionId = 0

    override fun readImpl() {
        sessionId = readD()
    }

    override suspend fun runImpl() {
        connection?.let { con ->
            when (con.sessionId) {
                sessionId -> {
                    con.state = State.IN_GAME
                    con.sendPacket(OutEnterWorldOk())
                    packetHandler.handle(OutEnterWorldOk.OP_CODE)
                        ?.let { pck -> con.sendPacket(pck) }
                }
                else -> {
                    log.error { "Sessions doesnt match: ${con.sessionId} != $sessionId" }
                    packetHandler.handle(OutAuthClientFail.OP_CODE, sessionId)
                        ?.let { pck -> con.close(pck, true) }
                }
            }
        }
    }

    companion object {
        const val OP_CODE = 6
    }
}