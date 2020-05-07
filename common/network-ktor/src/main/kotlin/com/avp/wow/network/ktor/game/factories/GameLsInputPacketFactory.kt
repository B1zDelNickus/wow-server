package com.avp.wow.network.ktor.game.factories

import com.avp.wow.network.ktor.game.ls.GameLsConnection
import com.avp.wow.network.ktor.game.ls.GameLsConnection.Companion.State
import com.avp.wow.network.ktor.game.ls.GameLsInputPacket
import io.ktor.util.KtorExperimentalAPI
import mu.KotlinLogging
import java.nio.ByteBuffer

@KtorExperimentalAPI
object GameLsInputPacketFactory {

    private val log = KotlinLogging.logger(this::class.java.name)

    /**
     * Reads one packet from given ByteBuffer
     * @param data
     * @param client
     * @return AionClientPacket object from binary data
     */
    fun define(data: ByteBuffer, client: GameLsConnection): GameLsInputPacket? {
        var msg: GameLsInputPacket? = null
        val state: State = client.state
        val id: Int = data.get().toInt() and 0xff
        when (state) {
            State.CONNECTED -> {
                when (id) {
                    0x08 -> {
                        //msg = CM_UPDATE_SESSION(data, client)
                    }
                    else -> {
                        unknownPacket(
                            state,
                            id
                        )
                    }
                }
            }
            State.AUTHED -> {
                when (id) {
                    //InLogin.OP_CODE -> { msg = InLogin(data, client) }
                    else -> {
                        unknownPacket(
                            state,
                            id
                        )
                    }
                }
            }
            State.NONE -> TODO()
        }
        return msg
    }

    /**
     * Logs unknown packet.
     * @param state
     * @param id
     */
    private fun unknownPacket(state: State, id: Int) {
        log.warn {
            String.format(
                "Unknown packet recived from Aion client: 0x%02X state=%s",
                id,
                state.toString()
            )
        }
    }

}