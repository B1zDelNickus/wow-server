package com.avp.wow.login.network.gs

import com.avp.wow.network.packet.BaseOutputPacket
import java.nio.ByteBuffer

abstract class LoginGsOutputPacket : BaseOutputPacket() {

    /**
     * Write and encrypt this packet data for given connection, to given buffer.
     * @param con
     */
    fun write(con: LoginGsConnection, buf: ByteBuffer) {

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
    protected abstract fun writeImpl(con: LoginGsConnection)

}