package com.avp.wow.network.client.game.input.activity

import com.avp.wow.network.client.game.GameServerConnection.Companion.State
import com.avp.wow.network.client.game.GameServerInputPacket
import com.avp.wow.network.client.game.output.OutClientLoginCheck
import com.avp.wow.network.client.game.output.activity.OutActivity5
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.delay

@KtorExperimentalAPI
class InActivity4(vararg states: State) : GameServerInputPacket(OP_CODE, states.toList()) {

    private var sessionId: Int = 0

    override fun readImpl() {
        sessionId = readD()
    }

    override suspend fun runImpl() {
        connection?.let { con ->
            when (con.sessionId) {
                sessionId -> {
                    log.debug { "C step 4" }
                    delay(100)
                    con.sendPacket(OutActivity5())
                }
                else -> {

                }
            }
        }
    }

    companion object {
        const val OP_CODE = 907
    }
}