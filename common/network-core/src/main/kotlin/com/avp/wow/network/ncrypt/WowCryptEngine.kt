package com.avp.wow.network.ncrypt

import java.nio.ByteBuffer
import kotlin.experimental.inv

class WowCryptEngine {

    private var key = INITIAL_KEY

    /**
     * A secret blowfish cipher
     */
    private val cipher = BlowfishCipher(blowfishKey = key)

    /**
     * Update the key for packet encryption/decryption with the Blowfish Cipher
     * @param newKey new Blowfish Key
     */
    fun updateKey(newKey: ByteArray) {
        key = newKey
    }

    fun decrypt(data: ByteBuffer, offset: Int = data.arrayOffset() + data.position(), length: Int = data.remaining()): Boolean {
        val arr = data.array()
        cipher.decipher(arr, offset, length)
        xorEncryptDecrypt(arr, offset, length)
        /**
         * Initial packet with key must probably
         * Key will be updated after packet parsing
         */
        return validateClientPacket(buf = data)
    }

    fun encrypt(data: ByteBuffer, offset: Int = data.arrayOffset() + data.position(), length: Int = data.remaining()) {
        val arr = data.array()
        xorEncryptDecrypt(arr, offset, length)
        cipher.cipher(arr, offset, length)
    }

    private fun xorEncryptDecrypt(data: ByteArray, offset: Int = 0, length: Int = data.size) {
        for (i in offset until (offset + length + 2)) {
            val a: Int = data[i].toInt()
            val b: Int = STATIC_XOR_KEY[i % STATIC_XOR_KEY.size].toInt()
            data[i] = (a xor b).toByte()
        }
    }

    private fun validateClientPacket(buf: ByteBuffer): Boolean {
        return (buf.getShort(0) == buf.getShort(3).inv()) && buf[2] == STATIC_SERVER_PACKET_CODE
    }

    companion object {

        private val INITIAL_KEY = byteArrayOf(
            0x6b.toByte(),
            0x60.toByte(),
            0xcb.toByte(),
            0x5b.toByte(),
            0x82.toByte(),
            0xce.toByte(),
            0x90.toByte(),
            0xb1.toByte(),
            0xcc.toByte(),
            0x2b.toByte(),
            0x6c.toByte(),
            0x55.toByte(),
            0x6c.toByte(),
            0x6c.toByte(),
            0x6c.toByte(),
            0x6c.toByte()
        )

        private val STATIC_XOR_KEY =
            "nKO/WctQ0AVLbpzfBkS6NevDYT8ourG5CRlmdjyJ72aswx4EPq1UgZhFMXH?3iI9"
                .toByteArray()

        const val STATIC_SERVER_PACKET_CODE: Byte = 0x56

        private const val MASK_1: Int = 0xD4
        private const val MASK_2: Int = 0xD5

        /**
         * Server packet opcodec obfuscation.
         * @param op
         * @return obfuscated opcodec
         */
        fun encodeOpCodec(op: Int) = op + MASK_1 xor MASK_2

        fun decodeOpCodec(op: Int) = (op xor MASK_2) - MASK_1

    }

}