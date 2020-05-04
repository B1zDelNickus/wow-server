package com.avp.wow.network.client.login

import com.avp.wow.network.BaseNioService
import com.avp.wow.network.KtorConnection
import com.avp.wow.network.client.factories.LoginServerInputPacketFactory
import com.avp.wow.network.ncrypt.CryptEngine
import com.avp.wow.network.ncrypt.EncryptedRSAKeyPair
import com.avp.wow.network.ncrypt.KeyGen
import io.ktor.network.sockets.Socket
import io.ktor.util.KtorExperimentalAPI
import javolution.util.FastList
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.crypto.SecretKey

@KtorExperimentalAPI
class LoginServerConnection(
    socket: Socket,
    nio: BaseNioService
) : KtorConnection(
    socket = socket,
    nio = nio,
    readBufferSize = DEFAULT_R_BUFFER_SIZE,
    writeBufferSize = DEFAULT_W_BUFFER_SIZE
) {

    var state = State.DEFAULT

    var sessionId = 0

    /**
     * Server Packet "to send" Queue
     */
    private val sendMsgQueue = FastList<LoginServerOutputPacket>()

    /**
     * Crypt to encrypt/decrypt packets
     */
    private var cryptEngine: CryptEngine? = null

    /**
     * Scrambled key pair for RSA
     */
    private var encryptedRSAKeyPair: EncryptedRSAKeyPair? = null

    override suspend fun dispatchRead() { read() }

    override suspend fun dispatchWrite() { write() }

    /**
     * Decrypt packet.
     * @param buf
     * @return true if success
     */
    private fun decrypt(buf: ByteBuffer): Boolean {
        val size = buf.remaining()
        val offset = buf.arrayOffset() + buf.position()
        val ret = cryptEngine?.decrypt(buf.array(), offset, size)
            ?: true // ???? // throw IllegalArgumentException("Crypt Engine was not initialized properly")
        if (!ret) { log.warn { "Wrong checksum from client: $this" } }
        return ret
    }

    /**
     * Encrypt packet.
     * @param buf
     * @return encrypted packet size.
     */
    fun encrypt(buf: ByteBuffer): Int {
        var size = buf.limit() - 2
        val offset = buf.arrayOffset() + buf.position()
        size = cryptEngine?.encrypt(buf.array(), offset, size)
            ?:  throw IllegalArgumentException("Crypt Engine was not initialized properly")
        return size
    }

    private suspend fun write() {
        var numWrite: Int
        val wb = writeBuffer
        /**
         * We have not writted data
         */
        if (wb.hasRemaining()) {
            try {
                numWrite = outputChannel.writeAvailable(wb)
            } catch (e: IOException) {
                //(nio as KtorNioClient).closeConnectionImpl(this) TODO
                return
            }
            if (numWrite == 0) {
                log.info("Write $numWrite ip: $ip")
                return
            }

            /**
             * Again not all data was send
             */
            if (wb.hasRemaining()) {
                return
            }
        }

        while (true) {
            wb.clear()
            val writeFailed = !writeData(wb)
            if (writeFailed) {
                wb.limit(0)
                break
            }
            /**
             * Attempt to write to the channel
             */
            try {
                numWrite = outputChannel.writeAvailable(wb)
            } catch (e: IOException) {
                //(nio as KtorNioServer).closeConnectionImpl(this) TODO
                return
            }
            if (numWrite == 0) {
                log.info("Write $numWrite ip: $ip")
                return
            }

            /**
             * not all data was send
             */
            if (wb.hasRemaining()) {
                return
            }
        }

        /**
         * We wrote all data so we can close connection that is "PandingClose"
         */
        if (isPendingClose) {
            // (nio as KtorNioServer).closeConnectionImpl(this) TODO
        }
    }

    private suspend fun read() {

        val rb = readBuffer
        /**
         * Attempt to read off the channel
         */
        val numRead: Int = try {
            inputChannel.readAvailable(rb)
        } catch (e: Exception) {
            // (nio as KtorNioServer).closeConnectionImpl(this) TODO
            return
        }

        when (numRead) {
            -1 -> {
                /**
                 * Remote entity shut the socket down cleanly. Do the same from our end and cancel the channel.
                 */
                // (nio as KtorNioServer).closeConnectionImpl(this) TODO
                return
            }
            0 -> return
        }

        rb.flip()

        while (rb.remaining() > 2 && rb.remaining() >= rb.getShort(rb.position())) {
            /**
             * got full message
             */
            if (!parse(rb)) {
                // (nio as KtorNioServer).closeConnectionImpl(this) TODO
                return
            }
        }

        when {
            rb.hasRemaining() -> readBuffer.compact()
            else -> rb.clear()
        }

    }

    private fun parse(buf: ByteBuffer): Boolean {
        var sz: Short = 0
        try {
            sz = buf.short
            if (sz > 1) {
                sz = (sz - 2).toShort()
            }
            val b = buf.slice().limit(sz.toInt()) as ByteBuffer
            //b.order(ByteOrder.LITTLE_ENDIAN)
            //b.order(ByteOrder.BIG_ENDIAN)
            /**
             * read message fully
             */
            log.trace { "Pkt size: $sz, real size: ${buf.remaining()}" }
            buf.position(buf.position() + sz)
            return processData(b)
        } catch (e: IllegalArgumentException) {
            log.warn(e) { "Error on parsing input from client - account: " + this + " packet size: " + sz + " real size:" + buf.remaining() }
            return false
        }

    }

    override fun close(forced: Boolean) {
        TODO("Not yet implemented")
    }

    override fun onlyClose(): Boolean {
        TODO("Not yet implemented")
    }

    override fun processData(data: ByteBuffer): Boolean {

        if (!decrypt(data)) {
            return false
        }

        val pck = LoginServerInputPacketFactory.define(data, this)

        /**
         * Execute packet only if packet exist (!= null) and read was ok.
         */
        if (pck != null && pck.read()) {
            log.debug { "Received packet $pck from login server." }
            //processor.executePacket(pck)

            // run sync for now ? fixme
            /*suspend {

                try {
                    withTimeout(1_000) {
                        pck.run()
                    }
                } catch (e: Exception) {
                    log.error(e) { "Too long packet execution" }
                }

            }*/

            GlobalScope.launch { pck.run() }

        }

        return true
    }

    fun sendPacket(packet: LoginServerOutputPacket) {
        synchronized(guard) {
            /**
             * Connection is already closed or waiting for last (close packet) to be sent
             */
            if (isWriteDisabled) {
                return
            }
            sendMsgQueue.addLast(packet)
        }
    }

    override fun writeData(data: ByteBuffer): Boolean {
        synchronized(guard) {
            val packet = try {
                sendMsgQueue.removeFirst()
            } catch (ignored: Exception) { return false }
            log.debug { "Send packet $packet to login server." }
            packet.write(this, data)
            return true
        }
    }

    override fun initialized() {
        state = State.CONNECTED
        log.info("Connected to server: [$ip]")
        encryptedRSAKeyPair = KeyGen.encryptedRSAKeyPair
    }

    override fun enableEncryption(blowfishKey: ByteArray) {
        cryptEngine = CryptEngine()
        cryptEngine!!.updateKey(blowfishKey)
    }

    override val getDisconnectionDelay = 0L

    override fun onDisconnect() {
        TODO("Not yet implemented")
    }

    override fun onServerClose() {
        TODO("Not yet implemented")
    }

    companion object {

        const val DEFAULT_R_BUFFER_SIZE = 8192 * 2
        const val DEFAULT_W_BUFFER_SIZE = 8192 * 2

        enum class State {

            /**
             * Default state
             */
            NONE,

            /**
             * Means that client just connects
             */
            CONNECTED,

            /**
             * Means that clients GameGuard is authenticated
             */
            AUTHED_GG,

            /**
             * Means that client is logged in.
             */
            AUTHED_LOGIN;

            companion object {

                val DEFAULT =
                    NONE

            }

        }

    }

}