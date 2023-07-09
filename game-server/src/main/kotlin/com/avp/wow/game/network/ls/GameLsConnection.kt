package com.avp.wow.game.network.ls

import com.avp.wow.game.GameServerConfig.processorMaxThreads
import com.avp.wow.game.GameServerConfig.processorMinThreads
import com.avp.wow.game.GameServerConfig.processorThreadKillThreshold
import com.avp.wow.game.GameServerConfig.processorThreadSpawnThreshold
import com.avp.wow.game.network.GameNioServer
import com.avp.wow.game.network.client.GameClientConnection
import com.avp.wow.game.network.factories.GameLsInputPacketFactory
import com.avp.wow.game.network.ls.GameLsConnection.Companion.State
import com.avp.wow.game.network.ls.output.OutAccountCheck
import com.avp.wow.network.BaseNioService
import com.avp.wow.network.BaseState
import com.avp.wow.network.KtorConnection
import com.avp.wow.network.KtorPacketProcessor
import com.avp.wow.network.ncrypt.WowCryptEngine
import io.ktor.network.sockets.*
import io.ktor.util.*
import javolution.util.FastList
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException
import java.nio.ByteBuffer
import kotlin.coroutines.CoroutineContext

class GameLsConnection(
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

    private val processor by lazy {
        KtorPacketProcessor<GameLsConnection>(
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
    private val sendMsgQueue = FastList<GameLsOutputPacket>()

    private val checkAccountRequests = mutableMapOf<Long, GameClientConnection>()
    val loggedInAccounts = mutableMapOf<Long, GameClientConnection>()

    var serverShutdown = false

    /**
     * Crypt to encrypt/decrypt packets
     */
    private val cryptEngine by lazy { WowCryptEngine() }

    private val inputPacketHandler by lazy { GameLsInputPacketFactory.packetHandler }

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
        val pck = inputPacketHandler.handle(data, this)
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
        //sendPacket(OutAuthGs())
    }

    override val disconnectionDelay: Long
        get() = TODO("Not yet implemented")

    override fun onDisconnect() {
        log.info { "Ls connection lost - disconnecting all clients pending auth from GS." }
        // TODO make clients close
        synchronized(checkAccountRequests) {
            checkAccountRequests.values.forEach { client ->
                client.close(forced = true)
            }
            checkAccountRequests.clear()
        }
        if (!serverShutdown) {
            scope.launch {
                delay(5_000) // TODO to ENV
                (nio as GameNioServer).connectLs()
            }
        }
    }

    override fun onServerClose() {
        close(forced = true)
    }

    override fun enableEncryption(blowfishKey: ByteArray) {
        cryptEngine.updateKey(newKey = blowfishKey)
    }

    fun requestAccountCheck(accountId: Long, loginOk: Int, playOk1: Int, playOk2: Int, gsc: GameClientConnection) {
        synchronized(checkAccountRequests) {
            if (checkAccountRequests.containsKey(accountId)) return
            checkAccountRequests[accountId] = gsc
        }
        sendPacket(
            OutAccountCheck(
                accountId = accountId,
                loginOk = loginOk,
                playOk1 = playOk1,
                playOk2 = playOk2
            )
        )
    }

    fun removeAccountCheckRequest(accountId: Long) =
        checkAccountRequests.remove(accountId)

    companion object {

        const val DEFAULT_R_BUFFER_SIZE = 8192 * 2
        const val DEFAULT_W_BUFFER_SIZE = 8192 * 2

        enum class State : BaseState {

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
                val DEFAULT = NONE
            }

        }

    }
}