package com.avp.wow.network.ktor.login.client

import com.avp.wow.network.packet.BaseServerPacket
import io.ktor.util.KtorExperimentalAPI
import java.nio.ByteBuffer

@KtorExperimentalAPI
abstract class ClientServerPacket : BaseServerPacket() {

    init {
        opCode = ServerPacketsOpcodes.getOpcode(this::class)
    }

    /**
     * Write and encrypt this packet data for given connection, to given buffer.
     * @param con
     */
    fun write(con: LoginConnection, buf: ByteBuffer) {

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

    }

    /**
     * Write data that this packet represents to given byte buffer.
     * @param con
     */
    protected abstract fun writeImpl(con: LoginConnection)

}