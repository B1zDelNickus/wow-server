package com.avp.wow.network.ktor.game.client.input

import com.avp.wow.network.ktor.game.GameNioServer
import com.avp.wow.network.ktor.game.client.GameClientConnection.Companion.State
import com.avp.wow.network.ktor.game.client.GameClientInputPacket
import com.avp.wow.network.ktor.game.client.output.OutAuthClientOk
import com.avp.wow.network.ktor.game.ls.GameLsConnection
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
class InClientLoginCheck(vararg states: State) : GameClientInputPacket(OP_CODE, states.toList()) {

    private var sessionId = 0
    private var accountId = 0L
    private var loginOk = 0
    private var playOk1 = 0
    private var playOk2 = 0

    override fun readImpl() {
        sessionId = readD()
        playOk2 = readD()
        playOk1 = readD()
        accountId = readQ()
        loginOk = readD()
    }

    override suspend fun runImpl() {
        connection?.let { con ->
            when (con.sessionId) {
                sessionId -> {
                    //con.state = State.AUTHED
                    //con.sendPacket(OutAuthClientOk())

                    val lsCon = (con.nio as GameNioServer).loginServerConnection

                    when {
                        null == lsCon || lsCon.state != GameLsConnection.Companion.State.REGISTERED -> {
                            log.warn { if (lsCon == null) "LS is NULL" else "LS state: ${lsCon.state}" }
                            // TODO some error packet
                            con.close(forced = true)
                        }
                        else -> {

                            lsCon.requestAccountCheck(
                                accountId = accountId,
                                loginOk = loginOk,
                                playOk1 = playOk1,
                                playOk2 = playOk2,
                                gsc = con
                            )

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