package com.avp.wow.game.network.client

import com.avp.wow.game.network.client.output.OutInitSession
import com.avp.wow.game.network.factories.GameClientInputPacketFactory
import com.avp.wow.model.auth.Account
import com.avp.wow.network.BaseNioService
import com.avp.wow.network.KtorConnection
import com.avp.wow.network.ncrypt.EncryptedRSAKeyPair
import com.avp.wow.network.ncrypt.KeyGen
import com.avp.wow.network.ncrypt.WowCryptEngine
import io.ktor.network.sockets.Socket
import io.ktor.util.KtorExperimentalAPI
import javolution.util.FastList
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import javax.crypto.SecretKey
import kotlin.coroutines.CoroutineContext

@KtorExperimentalAPI
class GameClientConnection(
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
    var sessionId = hashCode()
    var publicRsa: ByteArray? = null
    var account: Account? = null

    /**
     * Server Packet "to send" Queue
     */
    private val sendMsgQueue = FastList<GameClientOutputPacket>()

    /**
     * Crypt to encrypt/decrypt packets
     */
    private val cryptEngine by lazy { WowCryptEngine() }

    /**
     * Scrambled key pair for RSA
     */
    private var encryptedRSAKeyPair: EncryptedRSAKeyPair? = null
    /**
     * Return Scrambled modulus
     * @return Scrambled modulus
     */
    val encryptedModulus
        get() = encryptedRSAKeyPair?.rsaKeyPair?.public?.encoded
            ?: throw IllegalArgumentException("RSA key was not initialized properly")

    /**
     * Return RSA private key
     * @return rsa private key
     */
    val rsaPrivateKey
        get() = encryptedRSAKeyPair?.rsaKeyPair?.private
            ?: throw IllegalArgumentException("RSA key was not initialized properly")

    private val inputPacketHandler = GameClientInputPacketFactory.packetHandler

    override fun close(forced: Boolean) {
        TODO("Not yet implemented")
    }

    override fun onlyClose(): Boolean {
        TODO("Not yet implemented")
    }

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

    fun sendPacket(packet: GameClientOutputPacket) {
        log.debug { "Sending $packet" }
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

    override fun processData(data: ByteBuffer): Boolean {
        try {
            if (!cryptEngine.decrypt(data)) {
                log.debug { "Decrypt fail, client packet passed..." }
                return true
            }
        } catch (e: Exception) {
            log.error(e) { "Exception caught during decrypt - ${e.message}" }
            return false
        }

        if (data.remaining() < 5) { // op + static code + op == 5 bytes
            log.error("Received fake packet from: $this")
            return false
        }

        val pck = inputPacketHandler.handle(data, this)
        /**
         * Execute packet only if packet exist (!= null) and read was ok.
         */
        if (pck != null) {

            /// TODO flood protection

            if (pck.read()) {
                log.debug { "Received packet $pck from client: $ip" }
                //processor.executePacket(pck)

                scope.launch { pck.run() }
            }

        }
        return true
    }

    override fun writeData(data: ByteBuffer): Boolean {
        synchronized(guard) {
            val begin = System.nanoTime()
            try {
                val packet = try {
                    sendMsgQueue.removeFirst()
                } catch (ignored: Exception) {
                    return false
                }
                log.debug { "Send packet $packet to client: $ip" }
                packet.write(this, data)
                return true
            } finally {
                //RunnableStatsManager.handleStats(packet.getClass(), "runImpl()", System.nanoTime() - begin)
            }
        }
    }

    override fun initialized() {
        log.debug { "Accepted client from: [$ip]." }
        state = State.CONNECTED
        encryptedRSAKeyPair = KeyGen.encryptedRSAKeyPair
        val blowfishKey: SecretKey = KeyGen.generateBlowfishKey()
        sendPacket(OutInitSession(client = this, blowfishKey = blowfishKey))
    }

    override val disconnectionDelay: Long
        get() = TODO("Not yet implemented")

    override fun onDisconnect() {
        TODO("Not yet implemented")
    }

    override fun onServerClose() {
        TODO("Not yet implemented")
    }

    override fun enableEncryption(blowfishKey: ByteArray) {
        cryptEngine.updateKey(blowfishKey)
    }

    fun close(closePacket: GameClientOutputPacket, forced: Boolean) {
        synchronized(guard) {
            if (isWriteDisabled) {
                return
            }
            pendingClose = true
            isForcedClosing = forced
            sendMsgQueue.clear()
            sendMsgQueue.addLast(closePacket)
        }
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
             * client just connect
             */
            CONNECTED,
            /**
             * client is authenticated
             */
            AUTHED,
            /**
             * client is verified on LS
             */
            IN_GAME;

            companion object {

                val DEFAULT =
                    NONE

            }

        }

    }

}