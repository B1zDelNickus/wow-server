package com.avp.wow.network.ktor.login.client

import com.avp.wow.network.packet.BaseOutputPacket
import io.ktor.util.KtorExperimentalAPI
import java.nio.ByteBuffer

@KtorExperimentalAPI
abstract class LoginClientOutputPacket : BaseOutputPacket() {

    init {
        opCode = LoginClientOutputPacketsOpcodes.getOpcode(this::class)
    }

    /**
     * Write and encrypt this packet data for given connection, to given buffer.
     * @param con
     */
    fun write(con: LoginClientConnection, buf: ByteBuffer) {

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
    protected abstract fun writeImpl(con: LoginClientConnection)

}