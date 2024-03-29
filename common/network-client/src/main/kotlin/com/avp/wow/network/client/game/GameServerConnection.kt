package com.avp.wow.network.client.game

import com.avp.wow.network.BaseNioService
import com.avp.wow.network.BaseState
import com.avp.wow.network.KtorConnection
import com.avp.wow.network.client.factories.GameServerInputPacketFactory
import com.avp.wow.network.client.game.GameServerConnection.Companion.State
import com.avp.wow.network.ncrypt.WowCryptEngine
import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.isClosed
import io.ktor.util.KtorExperimentalAPI
import javolution.util.FastList
import kotlinx.coroutines.launch
import java.io.IOException
import java.nio.ByteBuffer
import kotlin.coroutines.CoroutineContext

@KtorExperimentalAPI
class GameServerConnection(
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

    override var state = State.DEFAULT

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

    private val inputPacketHandler by lazy { GameServerInputPacketFactory.packetHandler }

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
        log.debug { "Connected to game server: [$ip]." }
        state = State.CONNECTED
    }

    override val disconnectionDelay: Long
        get() = TODO("Not yet implemented")

    override fun onDisconnect() {
        log.info { "Disconnected from from GS: $ip." }
    }

    override fun onServerClose() {
        close(forced = true)
    }

    override fun enableEncryption(blowfishKey: ByteArray) {
        cryptEngine.updateKey(newKey = blowfishKey)
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