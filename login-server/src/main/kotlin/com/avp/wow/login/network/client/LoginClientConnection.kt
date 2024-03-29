package com.avp.wow.login.network.client

import com.avp.wow.login.LoginServerConfig.processorMaxThreads
import com.avp.wow.login.LoginServerConfig.processorMinThreads
import com.avp.wow.login.LoginServerConfig.processorThreadKillThreshold
import com.avp.wow.login.LoginServerConfig.processorThreadSpawnThreshold
import com.avp.wow.login.network.client.LoginClientConnection.Companion.State
import com.avp.wow.login.network.client.output.OutInitSession
import com.avp.wow.login.network.factories.LoginClientInputPacketFactory
import com.avp.wow.login.network.factories.LoginClientOutputPacketFactory
import com.avp.wow.model.auth.Account
import com.avp.wow.network.BaseNioService
import com.avp.wow.network.BaseState
import com.avp.wow.network.KtorConnection
import com.avp.wow.network.KtorPacketProcessor
import com.avp.wow.network.ncrypt.EncryptedRSAKeyPair
import com.avp.wow.network.ncrypt.KeyGen
import com.avp.wow.network.ncrypt.WowCryptEngine
import com.avp.wow.service.auth.AuthConfig.authService
import io.ktor.network.sockets.*
import javolution.util.FastList
import java.io.IOException
import java.nio.ByteBuffer
import javax.crypto.SecretKey
import kotlin.coroutines.CoroutineContext

class LoginClientConnection(
    socket: Socket,
    nio: BaseNioService,
    context: CoroutineContext
) : KtorConnection<State>(
    socket = socket,
    nio = nio,
    context = context,
    readBufferSize = DEFAULT_R_BUFFER_SIZE,
    writeBufferSize = DEFAULT_W_BUFFER_SIZE
) {

    override var state = State.NONE

    /**
     * Returns unique sessionId of this connection.
     * @return SessionId
     */
    var sessionId = hashCode()

    private val processor by lazy {
        KtorPacketProcessor<LoginClientConnection>(
            minThreads = processorMinThreads,
            maxThreads = processorMaxThreads,
            threadSpawnThreshold = processorThreadSpawnThreshold,
            threadKillThreshold = processorThreadKillThreshold,
            context = scope.coroutineContext
        )
    }

    /**
     * Server Packet "to send" Queue
     */
    private val sendMsgQueue = FastList<LoginClientOutputPacket>()

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

    var sessionKey: SessionKey? = null

    var account: Account? = null

    var joinedGs = false

    private val inputPacketHandler by lazy { LoginClientInputPacketFactory.packetHandler }

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

    /**
     * Sends LoginClientOutputPacket to this client.
     * @param packet LoginClientOutputPacket to be sent.
     */
    fun sendPacket(packet: LoginClientOutputPacket) {
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
        val pck = inputPacketHandler.handle(data, this)
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
            } catch (ignored: Exception) {
                return false
            }
            log.debug { "Send packet $packet to client: $ip" }
            packet.write(this, data)
            packet.afterWrite(this)
            return true
        }
    }

    override fun initialized() {
        state = State.CONNECTED

        log.info("Connection initialized from: [$ip].")

        encryptedRSAKeyPair = KeyGen.encryptedRSAKeyPair
        val blowfishKey: SecretKey = KeyGen.generateBlowfishKey()

        /**
         * Send Init packet
         */
        LoginClientOutputPacketFactory.packetHandler.handle(OutInitSession.OP_CODE, this, blowfishKey)
            ?.also { pck -> sendPacket(pck) }

    }

    override fun enableEncryption(blowfishKey: ByteArray) {
        cryptEngine.updateKey(blowfishKey)
    }

    override val disconnectionDelay = 0L

    override fun onDisconnect() {
        /**
         * Remove account only if not joined GameServer yet.
         */
        if (null != account && !joinedGs) {
            authService.accountsOnLs.remove(account!!.id!!)
        }
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
            nio.closeConnection(this)
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
                    nio.removeConnection(this)
                    log.info { "Connection from $ip was successfully closed: ${socket.isClosed}" }
                }
                closed = true
            } catch (ignored: IOException) {
            }
        }
        return true
    }

    fun close(closePacket: LoginClientOutputPacket, forced: Boolean = false) {
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

    fun closeNow() {
        close(false)
    }

    companion object {

        const val DEFAULT_R_BUFFER_SIZE = 8192 * 2
        const val DEFAULT_W_BUFFER_SIZE = 8192 * 2

        enum class State : BaseState {

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
                val DEFAULT = NONE
            }

        }

    }

}