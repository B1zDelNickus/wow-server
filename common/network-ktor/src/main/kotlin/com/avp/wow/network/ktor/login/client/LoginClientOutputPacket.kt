package com.avp.wow.network.ktor.login.client

import com.avp.wow.network.packet.BaseOutputPacket
import io.ktor.util.KtorExperimentalAPI
import java.nio.ByteBuffer

@KtorExperimentalAPI
abstract class LoginClientOutputPacket : BaseOutputPacket() {

    init {
        opCode =
            LoginClientOutputPacketsOpcodes.getOpcode(
                this::class
            )
    }

    /**
     * Write and encrypt this packet data for given connection, to given buffer.
     * @param con
     */
    fun write(con: LoginClientConnection, buf: ByteBuffer) {

        buffer = buf

        buffer?.let { b ->

            b.putShort(0.toShort())
            b.put(opCode.toByte())
            writeImpl(con)
            b.flip()
            b.putShort(0.toShort())
            val b2 = b.slice()
            val size = (con.encrypt(b2) + 2)/* + 2*/
            b.putShort(0, size.toShort())
            b.position(0).limit(size)

        }

    }

    /**
     * Write data that this packet represents to given byte buffer.
     * @param con
     */
    protected abstract fun writeImpl(con: LoginClientConnection)

    open fun afterWrite(con: LoginClientConnection) = Unit

}