package com.avp.wow.network.client.factories

import com.avp.wow.network.client.login.LoginServerConnection
import com.avp.wow.network.client.login.LoginServerConnection.Companion.State
import com.avp.wow.network.client.login.LoginServerInputPacket
import com.avp.wow.network.client.login.input.InAuthGuard
import com.avp.wow.network.client.login.input.InEnterGameServerOk
import com.avp.wow.network.client.login.input.InInitSession
import com.avp.wow.network.client.login.input.InLoginOk
import com.avp.wow.network.ncrypt.WowCryptEngine
import io.ktor.util.KtorExperimentalAPI
import mu.KotlinLogging
import java.nio.ByteBuffer

@KtorExperimentalAPI
object LoginServerInputPacketFactory {

    private val log = KotlinLogging.logger(this::class.java.name)

    /**
     * Reads one packet from given ByteBuffer
     * @param data
     * @param client
     * @return AionClientPacket object from binary data
     */
    fun define(data: ByteBuffer, server: LoginServerConnection): LoginServerInputPacket? {
        var msg: LoginServerInputPacket? = null
        val state: State = server.state
        val id: Int = WowCryptEngine.decodeOpCodec(data.short.toInt()) and 0xffff
        /* Second opcodec. */
        data.position(data.position() + 3)
        when (state) {
            State.CONNECTED -> {
                when (id) {
                    InInitSession.OP_CODE -> { msg = InInitSession(data, server) }
                    InAuthGuard.OP_CODE -> { msg = InAuthGuard(data, server) }
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
            State.AUTHED_GG -> {
                when (id) {
                    InLoginOk.OP_CODE -> { msg = InLoginOk(data, server) }
                    else -> {
                        unknownPacket(
                            state,
                            id
                        )
                    }
                }
            }
            State.AUTHED_LOGIN -> {
                when (id) {
                    InEnterGameServerOk.OP_CODE -> { msg = InEnterGameServerOk(data, server) }
                    0x02 -> {
                        //msg = CM_PLAY(data, client)
                    }
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