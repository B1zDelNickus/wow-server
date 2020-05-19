package com.avp.wow.network.ktor.game.client.input.activity

import com.avp.wow.network.ktor.game.client.GameClientConnection.Companion.State
import com.avp.wow.network.ktor.game.client.GameClientInputPacket
import com.avp.wow.network.ktor.game.client.output.OutAuthClientOk
import com.avp.wow.network.ktor.game.client.output.OutEnterWorldOk
import com.avp.wow.network.ktor.game.client.output.activity.OutActivity1
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.delay

@KtorExperimentalAPI
class InActivity1(vararg states: State) : GameClientInputPacket(OP_CODE, states.toList()) {

    private var sessionId = 0

    override fun readImpl() {
        sessionId = readD()
    }

    override suspend fun runImpl() {
        connection?.let { con ->
            when (con.sessionId) {
                sessionId -> {
                    log.debug { "S step 1" }
                    delay(100)
                    con.sendPacket(OutActivity1())
                }
                else -> {

                }
            }
        }
    }

    companion object {
        const val OP_CODE = 900
    }
}