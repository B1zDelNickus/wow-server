package com.avp.wow.network

import io.ktor.network.sockets.*
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.*
import java.io.IOException
import java.nio.ByteBuffer
import kotlin.coroutines.CoroutineContext

@KtorExperimentalAPI
abstract class KtxConnection(
    val socket: Socket,
    val nio: BaseNioService,
    context: CoroutineContext,
    readBufferSize: Int,
    writeBufferSize: Int
) : BaseConnection(context = context, readBufferSize = readBufferSize, writeBufferSize = writeBufferSize) {

    private val isActive get() = !socket.isClosed

    private val inputChannel by lazy { socket.openReadChannel() }
    private val outputChannel by lazy { socket.openWriteChannel(autoFlush = true) }

    /**
     * IP address of this Connection.
     */
    override val ip = socket.remoteAddress.toString().replace("/", "")

    /**
     * Dispatching channel read operations
     */
    private suspend fun dispatchRead() {
        read()
    }

    suspend fun startReadDispatching() {
        log.info { "Starting read dispatcher job" }
        while (isActive) {
            try {
                dispatchRead()
                synchronized(guard) {}
                /**
                 * Just small delay gap for performance optimization
                 */
                delay(10)
            } catch (ignored: CancellationException) {
            } catch (e: Exception) {
                log.error(e) { "Dispatch error: ${e.message}" }
            }
        }
        log.info { "Closing read dispatcher job" }
    }

    /**
     * Dispatching channel write operations
     */
    private suspend fun dispatchWrite() {
        write()
    }

    suspend fun startWriteDispatching() {
        log.info { "Starting write dispatcher job" }
        while (isActive) {
            try {
                dispatchWrite()
                synchronized(guard) {}
                /**
                 * Just small delay gap for performance optimization
                 */
                delay(10)
            } catch (ignored: CancellationException) {
            } catch (e: Exception) {
                log.error(e) { "Dispatch error: ${e.message}" }
            }
        }
        log.info { "Closing write dispatcher job" }
    }

    override suspend fun write() {
        var numWrite: Int
        val wb = writeBuffer
        /**
         * We have not writted data
         */
        if (wb.hasRemaining()) {
            try {
                numWrite = outputChannel.writeAvailable(wb)
            } catch (e: IOException) {
                closeConnectionImpl()
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
                closeConnectionImpl()
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
            closeConnectionImpl()
        }
    }

    override suspend fun read() {

        val rb = readBuffer

        /**
         * Attempt to read off the channel
         */
        val numRead: Int = try {
            inputChannel.readAvailable(rb)
        } catch (e: Exception) {
            closeConnectionImpl()
            return
        }

        when (numRead) {
            -1 -> {
                /**
                 * Remote entity shut the socket down cleanly. Do the same from our end and cancel the channel.
                 */
                closeConnectionImpl()
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
                closeConnectionImpl()
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

}