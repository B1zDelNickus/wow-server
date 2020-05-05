package com.avp.wow.network.ktor.login.client

import com.avp.wow.network.BaseNioService
import com.avp.wow.network.ktor.PacketProcessor
import com.avp.wow.network.KtorConnection
import com.avp.wow.network.ktor.KtorNioServer
import com.avp.wow.network.ktor.login.client.output.OutInitSession
import com.avp.wow.network.ktor.login.factories.LoginClientInputPacketFactory
import com.avp.wow.network.ncrypt.CryptEngine
import com.avp.wow.network.ncrypt.EncryptedRSAKeyPair
import com.avp.wow.network.ncrypt.KeyGen
import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.isClosed
import io.ktor.util.KtorExperimentalAPI
import javolution.util.FastList
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.crypto.SecretKey

@KtorExperimentalAPI
class LoginClientConnection(
    socket: Socket,
    nio: BaseNioService
) : KtorConnection(
    socket = socket,
    nio = nio,
    readBufferSize = DEFAULT_R_BUFFER_SIZE,
    writeBufferSize = DEFAULT_W_BUFFER_SIZE
) {

    var state =
        State.DEFAULT

    private val processor = PacketProcessor<LoginClientConnection>()

    /**
     * Server Packet "to send" Queue
     */
    private val sendMsgQueue = FastList<LoginClientOutputPacket>()

    /**
     * Crypt to encrypt/decrypt packets
     */
    private var cryptEngine: CryptEngine? = null
    /**
     * Scrambled key pair for RSA
     */
    private var encryptedRSAKeyPair: EncryptedRSAKeyPair? = null

    /**
     * Returns unique sessionId of this connection.
     * @return SessionId
     */
    val sessionId = hashCode()

    /**
     * Return Scrambled modulus
     * @return Scrambled modulus
     */
    val getEncryptedModulus
        get() = encryptedRSAKeyPair?.encryptedModulus
            ?: throw IllegalArgumentException("RSA key was not initialized properly")

    /**
     * Return RSA private key
     * @return rsa private key
     */
    val getRSAPrivateKey
        get() = encryptedRSAKeyPair?.rsaKeyPair?.private
            ?: throw IllegalArgumentException("RSA key was not initialized properly")

    override suspend fun dispatchRead() { read() }

    override suspend fun dispatchWrite() { write() }

    /**
     * Decrypt packet.
     * @param buf
     * @return true if success
     */
    private fun decrypt(buf: ByteBuffer): Boolean {
        /*val size = buf.remaining()
        val offset = buf.arrayOffset() + buf.position()
        val ret = cryptEngine?.decrypt(buf.array(), offset, size)
            ?: throw IllegalArgumentException("Crypt Engine was not initialized properly")
        if (!ret) { log.warn { "Wrong checksum from client: $this" } }
        return ret*/
        return true
    }

    /**
     * Encrypt packet.
     * @param buf
     * @return encrypted packet size.
     */
    fun encrypt(buf: ByteBuffer): Int {
        /*var size = buf.limit() - 2
        val offset = buf.arrayOffset() + buf.position()
        size = cryptEngine?.encrypt(buf.array(), offset, size)
            ?: size + 2 // while not encrypted // throw IllegalArgumentException("Crypt Engine was not initialized properly")
        return size*/
        return buf.limit()
    }

    /**
     * Sends AionServerPacket to this client.
     * @param bp AionServerPacket to be sent.
     */
    fun sendPacket(bp: LoginClientOutputPacket) {
        synchronized(guard) {
            /**
             * Connection is already closed or waiting for last (close packet) to be sent
             */
            if (isWriteDisabled) {
                return
            }
            sendMsgQueue.addLast(bp)
        }
    }

    override fun processData(data: ByteBuffer): Boolean {

        if (!decrypt(data)) {
            return false
        }

        val pck = LoginClientInputPacketFactory.define(data, this)

        /**
         * Execute packet only if packet exist (!= null) and read was ok.
         */
        if (pck != null && pck.read()) {
            log.debug { "Received packet $pck from client: $ip" }
            processor.executePacket(pck)
        }

        return true
    }

    override fun writeData(data: ByteBuffer): Boolean {
        synchronized(guard) {
            val packet = try {
                sendMsgQueue.removeFirst()
            } catch (ignored: Exception) { return false }
            log.debug { "Send packet $packet to client: $ip" }
            packet.write(this, data)
            packet.afterWrite(this)
            return true
        }
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
                (nio as KtorNioServer).closeConnectionImpl(this)
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
                (nio as KtorNioServer).closeConnectionImpl(this)
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
            (nio as KtorNioServer).closeConnectionImpl(this)
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
            (nio as KtorNioServer).closeConnectionImpl(this)
            return
        }

        when (numRead) {
            -1 -> {
                /**
                 * Remote entity shut the socket down cleanly. Do the same from our end and cancel the channel.
                 */
                (nio as KtorNioServer).closeConnectionImpl(this)
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
                (nio as KtorNioServer).closeConnectionImpl(this)
                return
            }
        }

        when {
            rb.hasRemaining() -> readBuffer.compact()
            else -> rb.clear()
        }

    }

    /**
     * Parse data from buffer and prepare buffer for reading just one packet - call processData(ByteBuffer b).
     * @param con Connection
     * @param buf Buffer with packet data
     * @return True if packet was parsed.
     */
    private fun parse(buf: ByteBuffer): Boolean {
        var sz: Short = 0
        try {
            sz = buf.short
            if (sz > 1) {
                sz = (sz - 2).toShort()
            }
            val b = buf.slice().limit(sz.toInt()) as ByteBuffer
            //b.order(ByteOrder.LITTLE_ENDIAN)
            /**
             * read message fully
             */
            log.trace { "Pkt received with size: $sz." }
            buf.position(buf.position() + sz)
            return processData(b)
        } catch (e: IllegalArgumentException) {
            log.warn(e) { "Error on parsing input from client - account: " + this + " packet size: " + sz + " real size:" + buf.remaining() }
            return false
        }

    }

    override fun initialized() {
        state = State.CONNECTED

        log.info("Connection initialized from: $ip, sending welcome packet.")

        encryptedRSAKeyPair = KeyGen.encryptedRSAKeyPair
        val blowfishKey: SecretKey = KeyGen.generateBlowfishKey()

        cryptEngine = CryptEngine()
        cryptEngine!!.updateKey(blowfishKey.encoded)

        /**
         * Send Init packet
         */
        sendPacket(OutInitSession(this, blowfishKey))
    }

    override fun enableEncryption(blowfishKey: ByteArray) {
        /*cryptEngine = CryptEngine()
        cryptEngine!!.updateKey(blowfishKey)*/
    }

    override val getDisconnectionDelay = 0L

    override fun onDisconnect() {
        /**
         * Remove account only if not joined GameServer yet.
         */
    }

    override fun onServerClose() {
        close(forced = true)
    }

    /**
     * Connection will be closed at some time [by Dispatcher Thread], after that onDisconnect() method will be called to clear all other things.
     * @param forced is just hint that getDisconnectionDelay() should return 0 so OnDisconnect() method will be called without any delay.
     */
    override fun close(forced: Boolean) {
        synchronized(guard) {
            if (isWriteDisabled) {
                return
            }
            isForcedClosing = forced
            (nio as KtorNioServer).closeConnection(this)
        }
    }

    /**
     * This will only close the connection without taking care of the rest. May be called only by Dispatcher Thread. Returns true if connection was not closed before.
     * @return true if connection was not closed before.
     */
    override fun onlyClose(): Boolean {
        synchronized(guard) {
            if (closed) {
                return false
            }
            try {
                if (!socket.isClosed) {
                    socket.close()
                    socket.dispose()
                    (nio as KtorNioServer).removeConnection(this)
                    log.info { "Connection from $ip was successfully closed: ${socket.isClosed}" }
                }
                closed = true
            } catch (ignored: IOException) {
            }
        }
        return true
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