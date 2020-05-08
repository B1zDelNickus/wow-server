package com.avp.wow.network.ktor.game.client

import com.avp.wow.network.ncrypt.Crypt
import com.avp.wow.network.packet.BaseOutputPacket
import io.ktor.util.KtorExperimentalAPI
import java.nio.ByteBuffer

@KtorExperimentalAPI
abstract class GameClientOutputPacket : BaseOutputPacket() {

    init {
        opCode = GameClientOutputPacketsOpcodes.getOpcode(this::class)
    }

    /**
     * Write and encrypt this packet data for given connection, to given buffer.
     * @param con
     */
    fun write(con: GameClientConnection, buf: ByteBuffer) {

        buffer = buf

        buffer?.let { b ->

            b.putShort(0.toShort())
            writeOP(opCode)
            writeImpl(con)
            b.flip()
            b.putShort(b.limit().toShort())
            val b2 = b.slice()
            b.position(0)
            con.encrypt(b2)

        }

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