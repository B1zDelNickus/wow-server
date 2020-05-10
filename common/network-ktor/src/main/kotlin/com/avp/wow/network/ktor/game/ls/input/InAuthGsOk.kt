package com.avp.wow.network.ktor.game.ls.input

import com.avp.wow.network.ktor.game.ls.GameLsConnection
import com.avp.wow.network.ktor.game.ls.GameLsConnection.Companion.State.AUTHED
import com.avp.wow.network.ktor.game.ls.GameLsInputPacket
import com.avp.wow.network.ktor.game.ls.output.OutRegisterGs
import io.ktor.util.KtorExperimentalAPI
import java.nio.ByteBuffer

@KtorExperimentalAPI
class InAuthGsOk(
    buffer: ByteBuffer,
    client: GameLsConnection
) : GameLsInputPacket(
    opCode = OP_CODE,
    client = client,
    buffer = buffer
) {

    private var sessionId: Int = 0

    override fun readImpl() {
        sessionId = readD()
    }

    override suspend fun runImpl() {
        connection?.let { con ->
            when (con.sessionId) {
                sessionId -> {
                    con.state = AUTHED
                    sendPacket(OutRegisterGs())
                }
                else -> {

                }
            }
        }
    }

    companion object {
        const val OP_CODE = 0x03
    }

}