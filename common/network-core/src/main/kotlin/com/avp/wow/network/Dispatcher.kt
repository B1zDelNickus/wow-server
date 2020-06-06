package com.avp.wow.network

import com.avp.wow.network.utils.KtxRunnable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.SelectableChannel
import java.nio.channels.SelectionKey
import java.nio.channels.SocketChannel
import java.nio.channels.spi.SelectorProvider

abstract class Dispatcher(
    private val name: String,
    private val scope: CoroutineScope
) : KtxRunnable {

    protected val log = KotlinLogging.logger(this::class.java.name)

    val selector by lazy { SelectorProvider.provider().openSelector()!! }

    protected val guard = Any()

    abstract fun dispatch()

    fun register(channel: SelectableChannel, ops: Int, acceptor: IAcceptor): SelectionKey {
        synchronized(guard) {
            selector.wakeup()
            return channel.register(selector, ops, acceptor)
        }
    }

    fun register(channel: SelectableChannel, ops: Int, connection: KtxConnection<*>) {
        synchronized(guard) {
            selector.wakeup()
            connection.key = channel.register(selector, ops, connection)
        }
    }

    /**
     * Accept new connection.
     * @param key
     */
    protected fun accept(key: SelectionKey) {
        try {
            (key.attachment() as IAcceptor).accept(key)
        } catch (e: Exception) {
            log.error(e) { "Error while accepting connection: ${e.message}" }
        }
    }

    override suspend fun run() {
        log.info { "Run dispatcher: '$name'" }
        while (true) {
            try {
                dispatch()
            } catch (e: Exception) {
                log.trace(e) { "Dispatch error: ${e.message}" }
            }
        }
    }

    /**
     * Add connection to pendingClose list, so this connection will be closed by this `Dispatcher` as soon as possible.
     * @param con
     * @see com.aionemu.commons.network.Dispatcher.closeConnection
     */
    abstract fun closeConnection(con: KtxConnection<*>)

    /**
     * Read data from socketChannel represented by SelectionKey key. Parse and Process data. Prepare buffer for next read.
     * @param key
     */
    protected fun read(key: SelectionKey) {
        val socketChannel = key.channel() as SocketChannel
        val con = key.attachment() as KtxConnection<*>
        val rb: ByteBuffer = con.readBuffer

        /**
         * Attempt to read off the channel
         */
        val numRead = try {
            socketChannel.read(rb)
        } catch (e: IOException) {
            closeConnectionImpl(con)
            return
        }

        when (numRead) {
            -1 -> {
                /**
                 * Remote entity shut the socket down cleanly. Do the same from our end and cancel the channel.
                 */
                closeConnectionImpl(con)
            }
            0 -> Unit
            else -> {
                rb.flip()
                while (rb.remaining() > 2 && rb.remaining() >= rb.getShort(rb.position())) {
                    /**
                     * got full message
                     */
                    if (!parse(con, rb)) {
                        closeConnectionImpl(con)
                        return
                    }
                }
                when {
                    rb.hasRemaining() -> {
                        con.readBuffer.compact()
                    }
                    else -> {
                        rb.clear()
                    }
                }
            }
        }
    }

    /**
     * Parse data from buffer and prepare buffer for reading just one packet - call processData(ByteBuffer b).
     * @param con Connection
     * @param buf Buffer with packet data
     * @return True if packet was parsed.
     */
    private fun parse(con: KtxConnection<*>, buf: ByteBuffer): Boolean {
        var sz: Short = 0
        return try {
            sz = buf.short
            if (sz > 1) {
                sz = (sz - 2).toShort()
            }
            val b = buf.slice().limit(sz.toInt()) as ByteBuffer
            b.order(ByteOrder.LITTLE_ENDIAN)
            /**
             * read message fully
             */
            buf.position(buf.position() + sz)
            con.processData(b)
        } catch (e: IllegalArgumentException) {
            log.warn(e) {
                "Error on parsing input from client - account: " + con + " packet size: " + sz + " real size:" + buf.remaining()
            }
            false
        }
    }

    /**
     * Write as much as possible data to socketChannel represented by SelectionKey key. If all data were written key write interest will be disabled.
     * @param key
     */
    protected fun write(key: SelectionKey) {
        val socketChannel = key.channel() as SocketChannel
        val con = key.attachment() as KtxConnection<*>
        var numWrite: Int
        val wb: ByteBuffer = con.writeBuffer
        /**
         * We have not writted data
         */
        if (wb.hasRemaining()) {
            numWrite = try {
                socketChannel.write(wb)
            } catch (e: IOException) {
                closeConnectionImpl(con)
                return
            }
            if (numWrite == 0) {
                log.info { "Write " + numWrite + " ip: " + con.ip }
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
            val writeFailed: Boolean = !con.writeData(wb)
            if (writeFailed) {
                wb.limit(0)
                break
            }
            /**
             * Attempt to write to the channel
             */
            numWrite = try {
                socketChannel.write(wb)
            } catch (e: IOException) {
                closeConnectionImpl(con)
                return
            }
            if (numWrite == 0) {
                log.info { "Write " + numWrite + " ip: " + con.ip }
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
         * We wrote away all data, so we're no longer interested in writing on this socket.
         */
        key.interestOps(key.interestOps() and SelectionKey.OP_WRITE.inv())
        /**
         * We wrote all data so we can close connection that is "PandingClose"
         */
        if (con.isPendingClose) {
            closeConnectionImpl(con)
        }
    }

    /**
     * Connection will be closed [onlyClose()] and onDisconnect() method will be executed on another thread [DisconnectionThreadPool] after getDisconnectionDelay() time in ms. This method may only be called by current Dispatcher Thread.
     * @param con
     */
    protected fun closeConnectionImpl(con: KtxConnection<*>) {
        if (con.onlyClose()) {
            scope.launch { DisconnectionTask(connection = con).run() }
        }
    }

}