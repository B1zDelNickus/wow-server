package com.avp.wow.network.ktor.login.factories

import com.avp.wow.network.ktor.login.client.LoginClientPacket
import com.avp.wow.network.ktor.login.client.LoginConnection
import com.avp.wow.network.ktor.login.client.LoginConnection.Companion.State
import com.avp.wow.network.ktor.login.client.tp.CpTestFastExecutePkt
import com.avp.wow.network.ktor.login.client.tp.CpTestSlowExecutePkt
import io.ktor.util.KtorExperimentalAPI
import mu.KotlinLogging
import java.nio.ByteBuffer

@KtorExperimentalAPI
object LoginPacketFactory {

    private val log = KotlinLogging.logger(this::class.java.name)

    /**
     * Reads one packet from given ByteBuffer
     * @param data
     * @param client
     * @return AionClientPacket object from binary data
     */
    fun define(data: ByteBuffer, client: LoginConnection): LoginClientPacket? {
        var msg: LoginClientPacket? = null
        val state: State = client.state
        val id: Int = data.get().toInt() and 0xff
        when (state) {
            State.CONNECTED -> {
                when (id) {
                    CpTestFastExecutePkt.OP_CODE -> { msg = CpTestFastExecutePkt(data, client) }
                    CpTestSlowExecutePkt.OP_CODE -> { msg = CpTestSlowExecutePkt(data, client) }
                    0x07 -> {
                        //msg = CM_AUTH_GG(data, client)
                    }
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
                    0x0B -> {
                        //msg = CM_LOGIN(data, client)
                    }
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
                    0x05 -> {
                        //msg = CM_SERVER_LIST(data, client)
                    }
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