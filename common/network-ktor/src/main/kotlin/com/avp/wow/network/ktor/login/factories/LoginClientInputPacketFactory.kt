package com.avp.wow.network.ktor.login.factories

import com.avp.wow.network.ktor.login.client.LoginClientInputPacket
import com.avp.wow.network.ktor.login.client.LoginClientConnection
import com.avp.wow.network.ktor.login.client.LoginClientConnection.Companion.State
import com.avp.wow.network.ktor.login.client.input.InAuthClient
import com.avp.wow.network.ktor.login.client.input.InEnterGameServer
import com.avp.wow.network.ktor.login.client.input.InGameServersList
import com.avp.wow.network.ktor.login.client.input.InLogin
import com.avp.wow.network.ktor.login.client.tp.CpTestFastExecutePkt
import com.avp.wow.network.ktor.login.client.tp.CpTestSlowExecutePkt
import com.avp.wow.network.ncrypt.WowCryptEngine
import io.ktor.util.KtorExperimentalAPI
import mu.KotlinLogging
import java.nio.ByteBuffer

@KtorExperimentalAPI
object LoginClientInputPacketFactory {

    private val log = KotlinLogging.logger(this::class.java.name)

    /**
     * Reads one packet from given ByteBuffer
     * @param data
     * @param client
     * @return AionClientPacket object from binary data
     */
    fun define(data: ByteBuffer, client: LoginClientConnection): LoginClientInputPacket? {
        var msg: LoginClientInputPacket? = null
        val state: State = client.state
        val id: Int = WowCryptEngine.decodeOpCodec(data.short.toInt()) and 0xffff
        /* Second opcodec. */
        data.position(data.position() + 3)
        when (state) {
            State.CONNECTED -> {
                when (id) {
                    InAuthClient.OP_CODE -> {
                        msg = InAuthClient(data, client)
                    }
                    0x08 -> {
                        //msg = CM_UPDATE_SESSION(data, client)
                    }
                    else -> {
                        unknownPacket(state, id)
                    }
                }
            }
            State.AUTHED_GG -> {
                when (id) {
                    InLogin.OP_CODE -> {
                        msg = InLogin(data, client)
                    }
                    else -> {
                        unknownPacket(state, id)
                    }
                }
            }
            State.AUTHED_LOGIN -> {
                when (id) {
                    InGameServersList.OP_CODE -> {
                        msg =
                            InGameServersList(data, client)
                    }
                    InEnterGameServer.OP_CODE -> {
                        msg = InEnterGameServer(data, client)
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