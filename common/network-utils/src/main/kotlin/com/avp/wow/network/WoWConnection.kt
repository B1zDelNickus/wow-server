package com.avp.wow.network

import io.ktor.network.sockets.*
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.*
import mu.KotlinLogging
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder

@KtorExperimentalAPI
abstract class WoWConnection(
    val socket: Socket,
    protected val nioServer: NioServer,
    readBufferSize: Int,
    writeBufferSize: Int
) {

    protected val log = KotlinLogging.logger(this::class.java.name)

    val isActive get() = !socket.isClosed

    protected val inputChannel by lazy { socket.openReadChannel() }
    protected val outputChannel by lazy { socket.openWriteChannel(autoFlush = true) }

    protected val readBuffer by lazy {
        ByteBuffer.allocate(readBufferSize)
            .apply {
                order(ByteOrder.BIG_ENDIAN)
            }!!
    }

    protected val writeBuffer by lazy {
        ByteBuffer.allocate(writeBufferSize)
            .apply {
                flip()
                order(ByteOrder.BIG_ENDIAN)
            }!!
    }

    /**
     * IP address of this Connection.
     */
    val ip = socket.remoteAddress.toString().replace("/", "")

    /**
     * True if OnDisconnect() method should be called immediately after this connection was closed.
     */
    protected var isForcedClosing = false

    /**
     * True if this connection should be closed after sending last server packet.
     */
    protected var pendingClose = false

    /**
     * True if this connection is already closed.
     */
    protected var closed = false

    /**
     * Object on witch some methods are synchronized
     */
    protected val guard = Any()

    /**
     * @return True if this connection is pendingClose and not closed yet.
     */
    val isPendingClose get() = pendingClose && !closed

    /**
     * @return True if write to this connection is possible.
     */
    protected val isWriteDisabled get() = pendingClose || closed

    /**
     * Used only for PacketProcessor synchronization purpose
     */
    private var locked = false

    /**
     * Dispatching channel read operations
     */
    protected abstract suspend fun dispatchRead()

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
    protected abstract suspend fun dispatchWrite()

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

    /**
     * Connection will be closed at some time [by Dispatcher Thread], after that onDisconnect() method will be called to clear all other things.
     * @param forced is just hint that getDisconnectionDelay() should return 0 so OnDisconnect() method will be called without any delay.
     */
    fun close(forced: Boolean = false) {
        synchronized(guard) {
            if (isWriteDisabled) {
                return
            }
            isForcedClosing = forced
            nioServer.closeConnection(this)
        }
    }

    /**
     * This will only close the connection without taking care of the rest. May be called only by Dispatcher Thread. Returns true if connection was not closed before.
     * @return true if connection was not closed before.
     */
    fun onlyClose(): Boolean {
        synchronized(guard) {
            if (closed) {
                return false
            }
            try {
                if (!socket.isClosed) {
                    socket.close()
                    socket.dispose()
                    nioServer.removeConnection(this)
                    log.info { "Connection from $ip was successfully closed: ${socket.isClosed}" }
                }
                closed = true
            } catch (ignored: IOException) {
            }
        }
        return true
    }

    /**
     * Used only for PacketProcessor synchronization purpose. Return true if locked successful - if wasn't locked before.
     * @return locked
     */
    open fun tryLockConnection() = when {
        locked -> false
        else -> true.also { locked = it }
    }

    /**
     * Used only for PacketProcessor synchronization purpose. Unlock this connection.
     */
    open fun unlockConnection() { locked = false }

    /**
     * @param data
     * @return True if data was processed correctly, False if some error occurred and connection should be closed NOW.
     */
    protected abstract fun processData(data: ByteBuffer): Boolean

    /**
     * This method will be called by Dispatcher, and will be repeated till return false.
     * @param data
     * @return True if data was written to buffer, False indicating that there are not any more data to write.
     */
    protected abstract fun writeData(data: ByteBuffer): Boolean

    /**
     * Called when AConnection object is fully initialized and ready to process and send packets. It may be used as hook for sending first packet etc.
     */
    abstract fun initialized()

    /**
     * This method is called by Dispatcher when connection is ready to be closed.
     * @return time in ms after witch onDisconnect() method will be called.
     */
    abstract fun getDisconnectionDelay(): Long

    /**
     * This method is called by Dispatcher to inform that this connection was closed and should be cleared. This method is called only once.
     */
    abstract fun onDisconnect()

    /**
     * This method is called by NioServer to inform that NioServer is shouting down. This method is called only once.
     */
    abstract fun onServerClose()

}