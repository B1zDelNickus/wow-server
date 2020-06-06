package com.avp.wow.network.client.game.input.activity

import com.avp.wow.network.client.game.GameServerConnection.Companion.State
import com.avp.wow.network.client.game.GameServerInputPacket
import com.avp.wow.network.client.game.output.OutClientLoginCheck
import com.avp.wow.network.client.game.output.activity.OutActivity4
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.delay

@KtorExperimentalAPI
class InActivity3(vararg states: State) : GameServerInputPacket(OP_CODE, states.toList()) {

    private var sessionId: Int = 0

    override fun readImpl() {
        sessionId = readD()
    }

    override suspend fun runImpl() {
        connection?.let { con ->
            when (con.sessionId) {
                sessionId -> {
                    log.debug { "C step 3" }
                    delay(100)
                    con.sendPacket(OutActivity4())
                }
                else -> {

                }
            }
        }
    }

    companion object {
        const val OP_CODE = 905
    }
}