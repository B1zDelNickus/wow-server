package com.avp.wow.network.packet

import com.avp.wow.network.BaseConnection
import com.avp.wow.network.ncrypt.WowCryptEngine
import com.avp.wow.network.utils.PrintUtils
import java.nio.ByteBuffer
import kotlin.experimental.inv

abstract class BaseOutputPacket : BasePacket {

    constructor() : super(type = Companion.PacketType.SERVER)

    constructor(opCode: Int) : super(type = Companion.PacketType.SERVER, opCode = opCode)

    fun writeOpCode(opCode: Int) {
        val oc = WowCryptEngine.encodeOpCodec(opCode)
        writeH(oc) //
        writeC(WowCryptEngine.STATIC_SERVER_PACKET_CODE)
        writeH(oc.toShort().inv())
    }

    /**
     * Write int to buffer.
     * @param value
     */
    fun writeD(value: Int) { buffer?.putInt(value) }

    /**
     * Write short to buffer.
     * @param value
     */
    fun writeH(value: Int) { buffer?.putShort(value.toShort()) }
    fun writeH(value: Short) { buffer?.putShort(value) }

    /**
     * Write byte to buffer.
     * @param value
     */
    fun writeC(value: Int) { buffer?.put(value.toByte()) }
    fun writeC(value: Byte) { buffer?.put(value) }

    /**
     * Write double to buffer.
     * @param value
     */
    fun writeDF(value: Double) { buffer?.putDouble(value) }

    /**
     * Write float to buffer.
     * @param value
     */
    fun writeF(value: Float) { buffer?.putFloat(value) }

    /**
     * Write long to buffer.
     * @param value
     */
    fun writeQ(value: Long) { buffer?.putLong(value) }

    /**
     * Write String to buffer
     * @param text
     */
    fun writeS(text: String?) {
        when (text) {
            null -> buffer?.putChar('\u0000')
            else -> {
                val len = text.length
                for (i in 0 until len) {
                    buffer?.putChar(text[i])
                }
                buffer?.putChar('\u0000')
            }
        }
    }

    /**
     * Write byte array to buffer.
     * @param data
     */
    fun writeB(data: ByteArray) { buffer?.put(data) }

    fun writeB(bytes: String) { writeB(PrintUtils.hex2bytes(bytes)) }

    fun resetBufferPosition() { buffer?.position(0) }
    fun limitBufferSize(): Int { return buffer?.limit()?:0 }
    fun remainingInBuffer(): Int { return buffer?.remaining()?:0 }
    fun flipBuffer() { buffer?.flip() }
    fun sliceBuffer(): ByteBuffer { return buffer?.slice()?:throw IllegalStateException("Buffer must be not null!") }

    open fun <T : BaseConnection<*>> afterWrite(con: T) = Unit

}