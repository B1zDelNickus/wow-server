package com.avp.wow.login.network.gs

import com.avp.wow.login.network.factories.LoginGsInputPacketFactory
import com.avp.wow.login.network.gs.output.OutInitSession
import com.avp.wow.model.gs.GameServer
import com.avp.wow.network.BaseNioService
import com.avp.wow.network.KtorConnection
import com.avp.wow.network.KtorPacketProcessor
import com.avp.wow.network.ncrypt.EncryptedRSAKeyPair
import com.avp.wow.network.ncrypt.KeyGen
import com.avp.wow.network.ncrypt.WowCryptEngine
import io.ktor.network.sockets.Socket
import io.ktor.util.KtorExperimentalAPI
import javolution.util.FastList
import java.nio.ByteBuffer
import javax.crypto.SecretKey
import kotlin.coroutines.CoroutineContext

@KtorExperimentalAPI
class LoginGsConnection(
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

    var gameServerInfo: GameServer? = null

    /**
     * Returns unique sessionId of this connection.
     * @return SessionId
     */
    var sessionId = hashCode()

    private val processor = KtorPacketProcessor<LoginGsConnection>(8, 1, 3, 3, scope.coroutineContext)

    /**
     * Server Packet "to send" Queue
     */
    private val sendMsgQueue = FastList<LoginGsOutputPacket>()

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

    /**
     * Crypt to encrypt/decrypt packets
     */
    private val cryptEngine by lazy { WowCryptEngine() }

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

    fun sendPacket(packet: LoginGsOutputPacket) {
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
        if (!decrypt(data)) {
            return false
        }
        if (data.remaining() < 5) { // op + static code + op == 5 bytes
            log.error("Received fake packet from: $this")
            return false
        }
        val pck = LoginGsInputPacketFactory.define(data, this)
        /**
         * Execute packet only if packet exist (!= null) and read was ok.
         */
        if (pck != null && pck.read()) {
            log.debug { "Received packet $pck from game server: $ip" }
            processor.executePacket(pck)
        }
        return true
    }

    override fun writeData(data: ByteBuffer): Boolean {
        val packet = try {
            sendMsgQueue.removeFirst()
        } catch (ignored: Exception) {
            return false
        }
        log.debug { "Send packet $packet to client: $ip" }
        packet.write(this, data)
        return true
    }

    override fun initialized() {
        log.info("GameServer connection initialized from: [$ip].")
        state = State.CONNECTED
        encryptedRSAKeyPair = KeyGen.encryptedRSAKeyPair
        val blowfishKey: SecretKey = KeyGen.generateBlowfishKey()
        /**
         * Send Init packet
         */
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

    companion object {

        const val DEFAULT_R_BUFFER_SIZE = 8192 * 2
        const val DEFAULT_W_BUFFER_SIZE = 8192 * 2

        enum class State {

            /**
             * Default state
             */
            NONE,

            /**
             * Means that GameServer just connect, but is not authenticated yet
             */
            CONNECTED,

            /**
             * GameServer is authenticated
             */
            AUTHED,

            /**
             * GameServer is registered
             */
            REGISTERED;

            companion object {

                val DEFAULT =
                    NONE

            }

        }

    }

}