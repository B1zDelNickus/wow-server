package com.avp.wow.network.ktor.game.ls

import com.avp.wow.network.BaseNioService
import com.avp.wow.network.KtorConnection
import com.avp.wow.network.KtorPacketProcessor
import com.avp.wow.network.ktor.game.GameNioServer
import com.avp.wow.network.ktor.game.factories.GameLsInputPacketFactory
import com.avp.wow.network.ktor.game.ls.output.OutGsAuth
import io.ktor.network.sockets.Socket
import io.ktor.util.KtorExperimentalAPI
import javolution.util.FastList
import java.io.IOException
import java.nio.ByteBuffer

@KtorExperimentalAPI
class GameLsConnection(
    socket: Socket,
    nio: BaseNioService
) : KtorConnection(
    socket = socket,
    nio = nio,
    readBufferSize = DEFAULT_R_BUFFER_SIZE,
    writeBufferSize = DEFAULT_W_BUFFER_SIZE
) {

    var state = State.DEFAULT

    private val processor = KtorPacketProcessor<GameLsConnection>()

    /**
     * Server Packet "to send" Queue
     */
    private val sendMsgQueue = FastList<GameLsOutputPacket>()

    override suspend fun dispatchRead() {
        read()
    }

    override suspend fun dispatchWrite() {
        write()
    }

    override fun close(forced: Boolean) {
        TODO("Not yet implemented")
    }

    override fun onlyClose(): Boolean {
        TODO("Not yet implemented")
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
                (nio as GameNioServer).closeConnectionImpl(this)
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
                (nio as GameNioServer).closeConnectionImpl(this)
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
            (nio as GameNioServer).closeConnectionImpl(this)
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
            (nio as GameNioServer).closeConnectionImpl(this)
            return
        }

        when (numRead) {
            -1 -> {
                /**
                 * Remote entity shut the socket down cleanly. Do the same from our end and cancel the channel.
                 */
                (nio as GameNioServer).closeConnectionImpl(this)
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
                (nio as GameNioServer).closeConnectionImpl(this)
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
        /**
         * send first packet - authentication.
         */
        sendPacket(OutGsAuth())
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