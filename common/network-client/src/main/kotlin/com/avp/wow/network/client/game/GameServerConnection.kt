package com.avp.wow.network.client.game

import com.avp.wow.network.BaseNioService
import com.avp.wow.network.KtorConnection
import com.avp.wow.network.client.KtorNioClient
import com.avp.wow.network.client.factories.GameServerInputPacketFactory
import com.avp.wow.network.ncrypt.Crypt
import com.avp.wow.network.ncrypt.WowCryptEngine
import io.ktor.network.sockets.Socket
import io.ktor.util.KtorExperimentalAPI
import javolution.util.FastList
import java.io.IOException
import java.nio.ByteBuffer
import kotlin.coroutines.CoroutineContext

@KtorExperimentalAPI
class GameServerConnection(
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

    /**
     * Server Packet "to send" Queue
     */
    private val sendMsgQueue = FastList<GameServerOutputPacket>()

    /**
     * Crypt to encrypt/decrypt packets
     */
    private val cryptEngine by lazy { WowCryptEngine() }

    private val inputPacketHandler = GameServerInputPacketFactory.packetHandler

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

    fun sendPacket(packet: GameServerOutputPacket) {
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
                log.debug { "Decrypt fail, server packet passed..." }
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
        log.debug { "Connected to game server: [$ip]." }
        state = State.CONNECTED
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
             * client just connect
             */
            CONNECTED,

            /**
             * client is authenticated
             */
            AUTHED,

            /**
             * client entered world.
             */
            IN_GAME;

            companion object {

                val DEFAULT =
                    NONE

            }

        }

    }

}