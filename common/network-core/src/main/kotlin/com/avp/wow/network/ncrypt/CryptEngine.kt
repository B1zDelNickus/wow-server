package com.avp.wow.network.ncrypt

import kotlin.experimental.and
import kotlin.random.Random

/**
 * Crypto engine for ecnrypting/decrypting packets, error handling and verifying checksum
 * @author EvilSpirit
 */
class CryptEngine {
    /**
     * A key
     */
    private var key = byteArrayOf(
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
    /**
     * Tells you whether the key is updated or not
     */
    private var updatedKey = false
    /**
     * A secret blowfish cipher
     */
    private val cipher: BlowfishCipher

    /**
     * Default constructor. Initialize the Blowfish Cipher with an initial static key to encrypt the first packet sent to the client
     */
    init {
        cipher = BlowfishCipher(key)
    }

    /**
     * Update the key for packet encryption/decryption with the Blowfish Cipher
     * @param newKey new Blowfish Key
     */
    fun updateKey(newKey: ByteArray, force: Boolean = false) {
        key = newKey
        if (force) updatedKey = true
    }

    /**
     * Decrypt given data
     * @param data byte array to be decrypted
     * @param offset byte array offset
     * @param length byte array length
     * @return true, if decrypted packet has valid checksum, false overwise
     */
    fun decrypt(data: ByteArray, offset: Int, length: Int): Boolean {
        if (!updatedKey) {
            encryptDecrypt(data, offset, length - 2)
            return true
        }
        cipher.decipher(data, offset, length)
        return verifyChecksum(data, offset, length)
    }

    /**
     * Encrypt given data
     * @param data byte array to be encrypted
     * @param offset byte array offset
     * @param length byte array length
     * @return length of encrypted byte array
     */
    fun encrypt(data: ByteArray, offset: Int, length: Int): Int {
        var len = length
        len += 4
        // the key is not updated, so the first packet should be encrypted with
        // initial key
        if (!updatedKey) {
            len = encryptDecrypt(data, offset, length) + 2
            cipher.updateKey(key)
            updatedKey = true
        } else {
            len += 8 - len % 8
            appendChecksum(data, offset, len)
            cipher.cipher(data, offset, len)
        }
        return len
    }

    private fun encryptDecrypt(data: ByteArray, offset: Int = 0, length: Int = data.size): Int {
        println("Offset: $offset, length: $length")
        for (i in offset until (offset + length + 2)) {
            val a: Int = data[i].toInt()
            val b: Int = key[i % key.size].toInt()
            data[i] = (a xor b).toByte()
        }
        return length
    }

    /**
     * Verify checksum in a packet
     * @param data byte array - encrypted packet
     * @param offset byte array offset
     * @param length byte array size
     * @return true, if checksum is ok, false overwise
     */
    private fun verifyChecksum(data: ByteArray, offset: Int, length: Int): Boolean {
        if (length and 3 != 0 || length <= 4) {
            return false
        }
        var chksum: Long = 0
        val count = length - 4
        var check: Long
        var i: Int = offset
        while (i < count) {
            check = (data[i] and 0xff.toByte()).toLong()
            check = check or (data[i + 1].toLong() shl 8 and 0xff00)
            check = check or (data[i + 2].toLong() shl 0x10 and 0xff0000)
            check = check or (data[i + 3].toLong() shl 0x18 and -0x1000000)
            chksum = chksum xor check
            i += 4
        }
        check = (data[i] and 0xff.toByte()).toLong()
        check = check or (data[i + 1].toLong() shl 8 and 0xff00)
        check = check or (data[i + 2].toLong() shl 0x10 and 0xff0000)
        check = check or (data[i + 3].toLong() shl 0x18 and -0x1000000)
        check = (data[i] and 0xff.toByte()).toLong()
        check = check or (data[i + 1].toLong() shl 8 and 0xff00)
        check = check or (data[i + 2].toLong() shl 0x10 and 0xff0000)
        check = check or (data[i + 3].toLong() shl 0x18 and -0x1000000)
        return 0L == chksum
    }

    /**
     * add checksum to the end of the packet
     * @param raw byte array - encrypted packet
     * @param offset byte array offset
     * @param length byte array size
     */
    private fun appendChecksum(raw: ByteArray, offset: Int, length: Int) {
        var chksum: Long = 0
        val count = length - 4
        var ecx: Long
        var i: Int = offset
        while (i < count) {
            ecx = (raw[i] and 0xff.toByte()).toLong()
            ecx = ecx or (raw[i + 1].toLong() shl 8 and 0xff00)
            ecx = ecx or (raw[i + 2].toLong() shl 0x10 and 0xff0000)
            ecx = ecx or (raw[i + 3].toLong() shl 0x18 and -0x1000000)
            chksum = chksum xor ecx
            i += 4
        }
        ecx = (raw[i] and 0xff.toByte()).toLong()
        ecx = ecx or (raw[i + 1].toLong() shl 8) and 0xff00
        ecx = ecx or (raw[i + 2].toLong() shl 0x10) and 0xff0000
        ecx = ecx or (raw[i + 3].toLong() shl 0x18) and -0x1000000
        raw[i] = (chksum and 0xff).toByte()
        raw[i + 1] = (chksum shr 0x08 and 0xff).toByte()
        raw[i + 2] = (chksum shr 0x10 and 0xff).toByte()
        raw[i + 3] = (chksum shr 0x18 and 0xff).toByte()
    }

    /**
     * First packet encryption with XOR key (integer - 4 bytes)
     * @param data byte array to be encrypted
     * @param offset byte array offset
     * @param length byte array length
     * @param key integer value as key
     */
    private fun encXORPass(data: ByteArray, offset: Int, length: Int, key: Int) {
        val stop = length - 8
        var pos = 4 + offset
        var edx: Int
        var ecx = key
        while (pos < stop) {
            edx = data[pos].toInt() and 0xFF
            edx = edx or (data[pos + 1].toInt() and 0xFF) shl 8
            edx = edx or (data[pos + 2].toInt() and 0xFF) shl 16
            edx = edx or (data[pos + 3].toInt() and 0xFF) shl 24
            ecx += edx
            edx = edx xor ecx
            data[pos++] = (edx and 0xFF).toByte()
            data[pos++] = (edx shr 8 and 0xFF).toByte()
            data[pos++] = (edx shr 16 and 0xFF).toByte()
            data[pos++] = (edx shr 24 and 0xFF).toByte()
        }
        data[pos++] = (ecx and 0xFF).toByte()
        data[pos++] = (ecx shr 8 and 0xFF).toByte()
        data[pos++] = (ecx shr 16 and 0xFF).toByte()
        data[pos] = (ecx shr 24 and 0xFF).toByte()
    }
}
