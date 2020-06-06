package com.avp.wow.network.client.login

import com.avp.wow.network.packet.BaseOutputPacket
import io.ktor.util.KtorExperimentalAPI
import java.nio.ByteBuffer

@KtorExperimentalAPI
abstract class LoginServerOutputPacket : BaseOutputPacket() {

    /*init {
        opCode = LoginServerOutputPacketsOpcodes.getOpcode(this::class)
    }*/

    /**
     * Write and encrypt this packet data for given connection, to given buffer.
     * @param con
     */
    fun write(con: LoginServerConnection, buf: ByteBuffer) {

        buffer = buf

        writeH(0)
        writeOpCode(opCode)
        writeImpl(con)
        flipBuffer()
        writeH(limitBufferSize())

        val toEncrypt = sliceBuffer()
        con.encrypt(toEncrypt)

        resetBufferPosition()

    }

    /**
     * Write data that this packet represents to given byte buffer.
     * @param con
     */
    protected abstract fun writeImpl(con: LoginServerConnection)

}