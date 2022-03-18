package com.avp.wow.network.ktx.login.client

import com.avp.wow.network.Dispatcher
import com.avp.wow.network.KtxConnection
import com.avp.wow.network.ktx.PacketProcessor
import com.avp.wow.network.ktx.login.client.output.SpInit
import com.avp.wow.network.ktx.login.factories.LoginClientInputPacketFactory
import com.avp.wow.network.ncrypt.CryptEngine
import com.avp.wow.network.ncrypt.EncryptedRSAKeyPair
import com.avp.wow.network.ncrypt.KeyGen
import com.fasterxml.uuid.Generators
import javolution.util.FastList
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import javax.crypto.SecretKey

class LoginClientConnection(
    socket: SocketChannel,
    dispatcher: Dispatcher
) : KtxConnection(
    socketChannel = socket,
    dispatcher = dispatcher,
    readBufferSize = DEFAULT_R_BUFFER_SIZE,
    writeBufferSize = DEFAULT_W_BUFFER_SIZE
) {

    /**
     * PacketProcessor for executing packets.
     */
    private val processor by lazy {
        PacketProcessor<LoginClientConnection>(1, 8, 50, 3)
    }

    /**
     * Server Packet "to send" Queue
     */
    private val sendMsgQueue = FastList<LoginClientOutputPacket>()

    /**
     * Unique Session Id of this connection
     */
    val sessionId = hashCode()

    /**
     * Account object for this connection. if state = AUTHED_LOGIN account cant be null.
     */
    private val account: Any? = null

    /**
     * Crypt to encrypt/decrypt packets
     */
    private var cryptEngine: CryptEngine? = null

    /**
     * True if this user is connecting to GS.
     */
    private val joinedGs = false

    /**
     * Scrambled key pair for RSA
     */
    private var encryptedRSAKeyPair: EncryptedRSAKeyPair? = null

    /**
     * Session Key for this connection.
     */
    var sessionKey: SessionKey? = null

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

    /**
     * Current state of this connection
     */
    var state: State = State.DEFAULT

    override fun processData(data: ByteBuffer): Boolean {

        if (!decrypt(data)) {
            log.debug { "Failed decrypt data from channel" }
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
            return true
        }
    }

    override suspend fun write() {
        TODO("Not yet implemented")
    }

    override suspend fun read() {
        TODO("Not yet implemented")
    }

    /**
     * Decrypt packet.
     * @param buf
     * @return true if success
     */
    private fun decrypt(buf: ByteBuffer): Boolean {
        val size = buf.remaining()
        val offset = buf.arrayOffset() + buf.position()
        val ret = cryptEngine?.decrypt(buf.array(), offset, size)
            ?: throw IllegalArgumentException("Crypt Engine was not initialized properly")
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
        /*val offset = buf.arrayOffset() + buf.position()
        size = cryptEngine?.encrypt(buf.array(), offset, size)
            ?: throw IllegalArgumentException("Crypt Engine was not initialized properly")*/
        return size
    }

    /**
     * Sends AionServerPacket to this client.
     * @param bp AionServerPacket to be sent.
     */
    fun sendPacket(pkt: LoginClientOutputPacket) {
        synchronized(guard) {
            /**
             * Connection is already closed or waiting for last (close packet) to be sent
             */
            if (isWriteDisabled) {
                return
            }
            log.debug("sending packet: $pkt")
            sendMsgQueue.addLast(pkt)
            enableWriteInterest()
        }

    }

    fun closeNow() { close(false) }

    override fun initialized() {
        state = State.CONNECTED

        log.info("Connection accepted from: [$ip]")

        encryptedRSAKeyPair = KeyGen.encryptedRSAKeyPair
        val blowfishKey: SecretKey = KeyGen.generateBlowfishKey()
        println(blowfishKey)
        println(blowfishKey.format)
        println(blowfishKey.encoded.map { it.toInt() }.joinToString(":"))
        cryptEngine = CryptEngine()
        cryptEngine!!.updateKey(blowfishKey.encoded)

        /**
         * Send Init packet
         */
        sendPacket(SpInit(this, blowfishKey))
    }

    override val disconnectionDelay = 0L

    override fun onDisconnect() {
        /**
         * Remove account only if not joined GameServer yet.
         */
        /**
         * Remove account only if not joined GameServer yet.
         */
        if (account != null && !joinedGs) {
            //AccountController.removeAccountOnLS(account)
            //AccountTimeController.updateOnLogout(account)
        }
    }

    override fun onServerClose() {
        close(forced = true)
    }

    override fun enableEncryption(blowfishKey: ByteArray) {
        TODO("Not yet implemented")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as LoginClientConnection
        if (hash != other.hash) return false
        return true
    }

    override fun hashCode(): Int {
        return hash.hashCode()
    }

    /**
     * @return String info about this connection
     */
    override fun toString(): String {
        return if (account != null) "$account - [$ip]" else "not logged - [$ip]"
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

                val DEFAULT = NONE

            }

        }

    }

}