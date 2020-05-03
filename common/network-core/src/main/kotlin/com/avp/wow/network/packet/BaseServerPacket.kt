package com.avp.wow.network.packet

import com.avp.wow.network.utils.PrintUtils

abstract class BaseServerPacket : BasePacket {

    constructor() : super(type = Companion.PacketType.SERVER)

    constructor(opCode: Int) : super(type = Companion.PacketType.SERVER, opCode = opCode)

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

    /**
     * Write byte to buffer.
     * @param value
     */
    fun writeC(value: Int) { buffer?.put(value.toByte()) }

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

}