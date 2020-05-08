package com.avp.wow.network.ncrypt

import mu.KotlinLogging
import java.nio.ByteBuffer
import kotlin.random.Random

class Crypt(private val isClientSide: Boolean) {

    private val log = KotlinLogging.logger(this::class.java.name)

    /**
     * Crypt is enabled after first server packet was send.
     */
    var isEnabled = false

    private var packetKey: EncryptionKeyPair? = null

    /**
     * Enable crypt key - generate random key that will be used to encrypt second server packet [first one is unencrypted] and decrypt client packets. This method is called from SM_KEY server packet, that packet sends key to aion client.
     * @return "false key" that should by used by aion client to encrypt/decrypt packets.
     */
    fun enableKey(): Int {
        if (packetKey != null) {
            throw Exception("Key Already Set Exception")
        }
        /** rnd key - this will be used to encrypt/decrypt packet  */
        val key: Int = Random.nextInt()
        packetKey = EncryptionKeyPair(key)
        log.debug { "new encrypt key: $packetKey" }
        /** false key that will be sent to aion client in SM_KEY packet  */
        return (key xor -0x326d1b2b) + 0x3FF2CCD7
    }

    /**
     * Decrypt client packet from this ByteBuffer.
     * @param buf
     * @return true if decryption was successful.
     */
    fun decrypt(buf: ByteBuffer): Boolean {
        if (!isEnabled) {
            log.debug { "if encryption wasn't enabled, then maybe it's client reconnection, so skip packet" }
            return true
        }
        return packetKey!!.decrypt(buf)
    }

    /**
     * Encrypt server packet from this ByteBuffer.
     * @param buf
     */
    fun encrypt(buf: ByteBuffer) {
        if (!isEnabled) {
            /** first packet is not encrypted  */
            isEnabled = true
            log.debug { "packet is not encrypted... send in SM_KEY" }
            return
        }
        packetKey!!.encrypt(buf)
    }

    companion object {

        const val STATIC_SERVER_PACKET_CODE: Byte = 0x56 // TODO maybe must be unique to every server

        /**
         * Server packet opcodec obfuscation.
         * @param op
         * @return obfuscated opcodec
         */
        fun encodeOpCodec(op: Int): Int {
            return op + 0xD4 xor 0xD5
        }

    }
}