package com.avp.wow.game.network.factories

import com.avp.wow.game.network.ls.GameLsConnection
import com.avp.wow.game.network.ls.GameLsConnection.Companion.State
import com.avp.wow.game.network.ls.GameLsInputPacket
import com.avp.wow.game.network.ls.input.*
import com.avp.wow.network.ncrypt.WowCryptEngine
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
        val id: Int = WowCryptEngine.decodeOpCodec(data.short.toInt()) and 0xffff
        /* Second opcodec. */
        data.position(data.position() + 3)
        when (state) {
            State.CONNECTED -> {
                when (id) {
                    InInitSession.OP_CODE -> {
                        msg = InInitSession(data, client)
                    }
                    InAuthGsOk.OP_CODE -> {
                        msg = InAuthGsOk(data, client)
                    }
                    else -> {
                        unknownPacket(state, id)
                    }
                }
            }
            State.AUTHED -> {
                when (id) {
                    InRegisterGsOk.OP_CODE -> {
                        msg = InRegisterGsOk(data, client)
                    }
                    else -> {
                        unknownPacket(state, id)
                    }
                }
            }
            State.REGISTERED -> {
                when (id) {
                    InAccountCheckResponse.OP_CODE -> {
                        msg = InAccountCheckResponse(data, client)
                    }
                    else -> {
                        unknownPacket(state, id)
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