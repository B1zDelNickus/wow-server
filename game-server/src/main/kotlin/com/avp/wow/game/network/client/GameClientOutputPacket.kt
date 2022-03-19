package com.avp.wow.game.network.client

import com.avp.wow.network.ncrypt.Crypt
import com.avp.wow.network.packet.BaseOutputPacket
import io.ktor.util.KtorExperimentalAPI
import java.nio.ByteBuffer

@KtorExperimentalAPI
abstract class GameClientOutputPacket : BaseOutputPacket() {

    /**
     * Write and encrypt this packet data for given connection, to given buffer.
     * @param con
     */
    fun write(con: GameClientConnection, buf: ByteBuffer) {

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

    private fun writeOP(value: Int) {
        /** obfuscate packet id  */
        buffer?.let { buf ->
            val op: Int = Crypt.encodeOpCodec(value)
            buf.putShort(op.toShort())
            /** put static server packet code  */
            buf.put(Crypt.STATIC_SERVER_PACKET_CODE)
            /** for checksum?  */
            buf.putShort(op.inv().toShort())
        }
    }

    protected fun writeS(text: String?, size: Int) {
        buffer?.let { buf ->
            if (text.isNullOrEmpty()) {
                buf.put(ByteArray(size))
            } else {
                val len = text.length
                for (i in 0 until len) {
                    buf.putChar(text[i])
                }
                buf.put(ByteArray(size - len * 2))
            }
        }
    }

    /**
     * Write data that this packet represents to given byte buffer.
     * @param con
     */
    protected abstract fun writeImpl(con: GameClientConnection)

}