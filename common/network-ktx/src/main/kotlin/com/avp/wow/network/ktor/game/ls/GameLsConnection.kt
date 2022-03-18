package com.avp.wow.network.ktor.game.ls

import com.avp.wow.network.BaseNioService
import com.avp.wow.network.KtxConnection
import com.avp.wow.network.KtxPacketProcessor
import com.avp.wow.network.ktor.game.factories.GameLsInputPacketFactory
import com.avp.wow.network.ktor.game.ls.output.OutAuthGs
import com.avp.wow.network.ncrypt.WowCryptEngine
import io.ktor.network.sockets.Socket
import io.ktor.util.KtorExperimentalAPI
import javolution.util.FastList
import java.nio.ByteBuffer
import kotlin.coroutines.CoroutineContext

@KtorExperimentalAPI
class GameLsConnection(
    socket: Socket,
    nio: BaseNioService,
    context: CoroutineContext
) : KtxConnection(
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

    private val processor = KtxPacketProcessor<GameLsConnection>()

    /**
     * Server Packet "to send" Queue
     */
    private val sendMsgQueue = FastList<GameLsOutputPacket>()

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

    fun sendPacket(packet: GameLsOutputPacket) {
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
        val pck = GameLsInputPacketFactory.define(data, this)
        /**
         * Execute packet only if packet exist (!= null) and read was ok.
         */
        if (pck != null && pck.read()) {
            log.debug { "Received packet $pck from login server: $ip" }
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
            log.debug { "Send packet $packet to login server: $ip" }
            packet.write(this, data)
            return true
        }
    }

    override fun initialized() {
        state = State.CONNECTED
        /**
         * send first packet - authentication.
         */
        sendPacket(OutAuthGs())
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
        cryptEngine.updateKey(newKey = blowfishKey)
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
            AUTHED;

            companion object {

                val DEFAULT =
                    NONE

            }

        }

    }
}