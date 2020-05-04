package com.avp.wow.network.ktx.login.client

import com.avp.wow.network.packet.BaseOutputPacket
import java.nio.ByteBuffer

abstract class LoginClientOutputPacket : BaseOutputPacket() {

    init {
        opCode = LoginClientOutputPacketsOpcodes.getOpcode(this::class)
    }

    /**
     * Write and encrypt this packet data for given connection, to given buffer.
     * @param con
     */
    fun write(con: LoginClientConnection, buf: ByteBuffer) {

        buf.putShort(0.toShort())
        buf.put(opCode.toByte())
        writeImpl(con)
        buf.flip()
        buf.putShort(0.toShort())
        val b = buf.slice()
        val size = (con.encrypt(b) + 2)
        buf.putShort(0, size.toShort())
        buf.position(0).limit(size)

        buffer = buf

        log.trace { "Wrote pkt $this with size: $size" }

    }

    /**
     * Write data that this packet represents to given byte buffer.
     * @param con
     */
    protected abstract fun writeImpl(con: LoginClientConnection)

}