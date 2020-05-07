package com.avp.wow.network.ktor.login.gs

import com.avp.wow.network.packet.BaseOutputPacket
import io.ktor.util.KtorExperimentalAPI
import java.nio.ByteBuffer

@KtorExperimentalAPI
abstract class LoginGsOutputPacket : BaseOutputPacket() {

    init {
        opCode = LoginGsOutputPacketsOpcodes.getOpcode(this::class)
    }

    /**
     * Write and encrypt this packet data for given connection, to given buffer.
     * @param con
     */
    fun write(con: LoginGsConnection, buf: ByteBuffer) {

        buffer = buf

        buffer?.let { b ->

            b.putShort(0.toShort())
            b.put(opCode.toByte())
            writeImpl(con)
            b.flip()
            b.putShort(b.remaining().toShort())
            b.position(0)

        }

    }

    /**
     * Write data that this packet represents to given byte buffer.
     * @param con
     */
    protected abstract fun writeImpl(con: LoginGsConnection)

}