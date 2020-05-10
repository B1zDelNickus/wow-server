package com.avp.wow.network.client.game.input

import com.avp.wow.network.client.game.GameServerConnection.Companion.State
import com.avp.wow.network.client.game.GameServerInputPacket
import com.avp.wow.network.client.game.output.OutClientLoginCheck
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
class InClientLoginCheckResponse(vararg states: State) : GameServerInputPacket(OP_CODE, states.toList()) {

    private var sessionId: Int = 0
    private var result: Boolean = false
    private var accountName: String = ""

    override fun readImpl() {
        sessionId = readD()
        result = readC() == 1
        accountName = readS()
    }

    override suspend fun runImpl() {
        connection?.let { con ->
            when (con.sessionId) {
                sessionId -> {

                    if (result) {
                        con.state = State.AUTHED
                        log.info { "Checked In to GS with account: $accountName" }
                    } else {
                        log.info { "Check In to GS failed, close connection and shoe Login Screen." }
                    }

                }
                else -> {

                }
            }
        }
    }

    companion object {
        const val OP_CODE = 0x05
    }
}