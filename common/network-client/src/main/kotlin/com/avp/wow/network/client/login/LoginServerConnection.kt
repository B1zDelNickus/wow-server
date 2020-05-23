package com.avp.wow.network.client.login

import com.avp.wow.network.BaseNioService
import com.avp.wow.network.KtorConnection
import com.avp.wow.network.client.factories.LoginServerInputPacketFactory
import com.avp.wow.network.ncrypt.CryptEngine
import com.avp.wow.network.ncrypt.EncryptedRSAKeyPair
import com.avp.wow.network.ncrypt.KeyGen
import com.avp.wow.network.ncrypt.WowCryptEngine
import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.isClosed
import io.ktor.util.KtorExperimentalAPI
import javolution.util.FastList
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.crypto.SecretKey
import kotlin.coroutines.CoroutineContext

@KtorExperimentalAPI
class LoginServerConnection(
    socket: Socket,
    nio: BaseNioService,
    context: CoroutineContext
) : KtorConnection(
    socket = socket,
    nio = nio,
    context = context,
    readBufferSize = DEFAULT_R_BUFFER_SIZE,
    writeBufferSize = DEFAULT_W_BUFFER_SIZE
) {

    var state = State.DEFAULT

    /**
     * Returns unique sessionId of this connection.
     * @return SessionId
     */
    var sessionId = 0
    var publicRsa: ByteArray? = null

    var accountId = 0L
    var loginOk = 0
    var playOk1 = 0
    var playOk2 = 0

    /**
     * Server Packet "to send" Queue
     */
    private val sendMsgQueue = FastList<LoginServerOutputPacket>()

    /**
     * Crypt to encrypt/decrypt packets
     */
    private val cryptEngine = WowCryptEngine()

    /**
     * Scrambled key pair for RSA
     */
    private var encryptedRSAKeyPair: EncryptedRSAKeyPair? = null

    /**
     * Decrypt packet.
     * @param buf
     * @return true if success
     */
    private fun decrypt(buf: ByteBuffer): Boolean {
        return cryptEngine.decrypt(data = buf)
    }

    /**
     * Encrypt packet.
     * @param buf
     * @return encrypted packet size.
     */
    fun encrypt(buf: ByteBuffer) {
        cryptEngine.encrypt(data = buf)
    }

    override fun close(forced: Boolean) {
        synchronized(guard) {
            if (isWriteDisabled) {
                return
            }
            isForcedClosing = forced
            nio.closeConnection(this)
        }
    }

    override fun onlyClose(): Boolean {
        synchronized(guard) {
            if (closed) {
                return false
            }
            try {
                if (!socket.isClosed) {
                    socket.close()
                    socket.dispose()
                    nio.removeConnection(this)
                    log.info { "Connection from $ip was successfully closed: ${socket.isClosed}" }
                }
                closed = true
            } catch (ignored: IOException) {
            }
        }
        return true
    }

    override fun processData(data: ByteBuffer): Boolean {
        if (!decrypt(data)) {
            return false
        }
        if (data.remaining() < 5) { // op + static code + op == 5 bytes
            log.error("Received fake packet from: $this")
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
        cryptEngine.updateKey(blowfishKey)
    }

    override val disconnectionDelay = 0L

    override fun onDisconnect() {
        log.info { "LS connection lost!" }
    }

    override fun onServerClose() {
        close(forced = true)
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