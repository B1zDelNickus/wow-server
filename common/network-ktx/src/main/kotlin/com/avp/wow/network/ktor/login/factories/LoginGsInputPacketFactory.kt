package com.avp.wow.network.ktor.login.factories

import com.avp.wow.network.ktor.login.gs.LoginGsConnection.Companion.State
import com.avp.wow.network.ktor.login.gs.LoginGsConnection
import com.avp.wow.network.ktor.login.gs.LoginGsInputPacket
import com.avp.wow.network.ktor.login.gs.input.InAuthGs
import com.avp.wow.network.ncrypt.WowCryptEngine
import io.ktor.util.KtorExperimentalAPI
import mu.KotlinLogging
import java.nio.ByteBuffer

@KtorExperimentalAPI
object LoginGsInputPacketFactory {

    private val log = KotlinLogging.logger(this::class.java.name)

    /**
     * Reads one packet from given ByteBuffer
     * @param data
     * @param client
     * @return AionClientPacket object from binary data
     */
    fun define(data: ByteBuffer, client: LoginGsConnection): LoginGsInputPacket? {
        var msg: LoginGsInputPacket? = null
        val state: State = client.state
        val id: Int = WowCryptEngine.decodeOpCodec(data.short.toInt()) and 0xffff
        /* Second opcodec. */
        data.position(data.position() + 3)
        when (state) {
            State.CONNECTED -> {
                when (id) {
                    InAuthGs.OP_CODE -> { msg = InAuthGs(data, client) }
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