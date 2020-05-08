package com.avp.wow.network.ncrypt

import java.nio.ByteBuffer
import kotlin.experimental.and
import kotlin.experimental.inv
import kotlin.experimental.xor

class EncryptionKeyPair(baseKey: Int) {

    /**
     * keys index to access SERVER encryption key
     */
    private val SERVER = 0

    /**
     * keys index to access CLIENT encryption key
     */
    private val CLIENT = 1

    /**
     * Static xor key
     */
    private val staticKey =
        "nKO/WctQ0AVLbpzfBkS6NevDYT8ourG5CRlmdjyJ72aswx4EPq1UgZhFMXH?3iI9".toByteArray()

    /**
     * Second byte of client packet must be equal to this
     */
    private val staticClientPacketCode: Byte = 0x7D

    /**
     * Base key used to generate client/server keys
     */
    private var baseKey = 0

    /**
     * Encryption keys
     */
    private var keys: Array<ByteArray?>? = null

    /**
     * Date of last key use
     */
    private var lastUpdate: Long = 0

    /**
     * Initializes client/server encryption keys based on baseKey
     * @param baseKey random integer
     */
    init {
        this.baseKey = baseKey
        keys = arrayOfNulls(2)
        keys!![SERVER] = byteArrayOf(
            (baseKey and 0xff).toByte(),
            (baseKey shr 8 and 0xff).toByte(),
            (baseKey shr 16 and 0xff).toByte(),
            (baseKey shr 24 and 0xff).toByte(),
            0xa1.toByte(),
            0x6c.toByte(),
            0x54.toByte(),
            0x87.toByte()
        )
        keys!![CLIENT] = ByteArray(keys!![SERVER]!!.size)
        System.arraycopy(keys!![SERVER]!!, 0, keys!![CLIENT]!!, 0, keys!![SERVER]!!.size)
        lastUpdate = System.currentTimeMillis()
    }

    /**
     * @return the baseKey used to generate the key pair
     */
    fun getBaseKey(): Int {
        return baseKey
    }

    /**
     * {@inheritDoc}
     */
    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("{client:0x")
        for (element in keys!![CLIENT]!!) {
            sb.append(Integer.toHexString(element.toInt() and 0xff))
        }
        sb.append(",server:0x")
        for (element in keys!![SERVER]!!) {
            sb.append(Integer.toHexString(element.toInt() and 0xff))
        }
        sb.append(",base:0x")
        sb.append(Integer.toHexString(baseKey))
        sb.append(",update:$lastUpdate}")
        return sb.toString()
    }

    /**
     * Check if packet was correctly decoded, also check if packet was correctly coded by aion client
     * @param buf
     * @return
     */
    private fun validateClientPacket(buf: ByteBuffer): Boolean {
        return (buf.getShort(0) == buf.getShort(3).inv()) && buf[2] == staticClientPacketCode
    }

    /**
     * Decrypt client packet from this ByteBuffer If decryption is successful, update client key
     * @param buf
     * @return true if decryption was successful
     */
    fun decrypt(buf: ByteBuffer): Boolean {
        val data = buf.array()
        val size = buf.remaining()
        val clientPacketKey = keys!![CLIENT]!!

        /** index to byte that should be decrypted now  */
        var arrayIndex = buf.arrayOffset() + buf.position()

        /** prev encrypted byte  */
        var prev = data[arrayIndex].toInt()
        /** decrypt first byte  */
        data[arrayIndex++] = data[arrayIndex++] xor (clientPacketKey[0] and 0xff.toByte())
        /** decrypt loop  */
        var i = 1
        while (i < size) {
            val curr: Int = data[arrayIndex].toInt() and 0xff
            data[arrayIndex] =
                data[arrayIndex] xor (staticKey[i and 63] and 0xff.toByte() xor (clientPacketKey[i and 7] and 0xff.toByte()) xor prev.toByte())
            prev = curr
            i++
            arrayIndex++
        }
        /** oldKey value as long  */
        var oldKey =
            clientPacketKey!![0].toLong() and 0xff shl 0 or (clientPacketKey[1]
                .toLong() and 0xff shl 8) or (clientPacketKey[2].toLong() and 0xff shl 16) or (clientPacketKey[3]
                .toLong() and 0xff shl 24) or (clientPacketKey[4]
                .toLong() and 0xff shl 32) or (clientPacketKey[5].toLong() and 0xff shl 40) or (clientPacketKey[6]
                .toLong() and 0xff shl 48) or (clientPacketKey[7].toLong() and 0xff shl 56)
        /** change key  */
        oldKey += size.toLong()
        if (validateClientPacket(buf)) {
            /** set key new value  */
            clientPacketKey[0] = (oldKey shr 0 and 0xff).toByte()
            clientPacketKey[1] = (oldKey shr 8 and 0xff).toByte()
            clientPacketKey[2] = (oldKey shr 16 and 0xff).toByte()
            clientPacketKey[3] = (oldKey shr 24 and 0xff).toByte()
            clientPacketKey[4] = (oldKey shr 32 and 0xff).toByte()
            clientPacketKey[5] = (oldKey shr 40 and 0xff).toByte()
            clientPacketKey[6] = (oldKey shr 48 and 0xff).toByte()
            clientPacketKey[7] = (oldKey shr 56 and 0xff).toByte()
            return true
        }
        return false
    }

    /**
     * Encrypt server packet from this ByteBuffer
     * @param buf
     */
    fun encrypt(buf: ByteBuffer) {
        val data = buf.array()
        val size = buf.remaining()
        val serverPacketKey = keys!![SERVER]!!

        /** index to byte that should be encrypted now  */
        var arrayIndex = buf.arrayOffset() + buf.position()
        /** encrypt first byte  */
        data[arrayIndex] = data[arrayIndex] xor (serverPacketKey[0] and 0xff.toByte())
        /** prev encrypted byte  */
        var prev = data[arrayIndex++].toInt()

        /** encrypt loop  */
        var i = 1
        while (i < size) {
            data[arrayIndex] =
                data[arrayIndex] xor (staticKey[i and 0x3F] and 0xff.toByte() xor (serverPacketKey!![i and 0x07] and 0xff.toByte()) xor prev.toByte())
            prev = data[arrayIndex].toInt()
            i++
            arrayIndex++
        }
        /** oldKey value as long  */
        var oldKey =
            serverPacketKey[0]
                .toLong() and 0xff shl 0 or (serverPacketKey[1]
                .toLong() and 0xff shl 8) or (serverPacketKey[2]
                .toLong() and 0xff shl 16) or (serverPacketKey[3]
                .toLong() and 0xff shl 24) or (serverPacketKey[4]
                .toLong() and 0xff shl 32) or (serverPacketKey[5]
                .toLong() and 0xff shl 40) or (serverPacketKey[6]
                .toLong() and 0xff shl 48) or (serverPacketKey[7]
                .toLong() and 0xff shl 56)
        /** change key  */
        oldKey += size.toLong()
        /** set key new value  */
        serverPacketKey[0] = (oldKey shr 0 and 0xff).toByte()
        serverPacketKey[1] = (oldKey shr 8 and 0xff).toByte()
        serverPacketKey[2] = (oldKey shr 16 and 0xff).toByte()
        serverPacketKey[3] = (oldKey shr 24 and 0xff).toByte()
        serverPacketKey[4] = (oldKey shr 32 and 0xff).toByte()
        serverPacketKey[5] = (oldKey shr 40 and 0xff).toByte()
        serverPacketKey[6] = (oldKey shr 48 and 0xff).toByte()
        serverPacketKey[7] = (oldKey shr 56 and 0xff).toByte()
    }

}