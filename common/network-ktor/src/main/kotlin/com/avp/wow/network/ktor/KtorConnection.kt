package com.avp.wow.network.ktor

import com.avp.wow.network.BaseConnection
import io.ktor.network.sockets.*
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.*
import mu.KotlinLogging
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder

@KtorExperimentalAPI
abstract class KtorConnection(
    val socket: Socket,
    protected val nioServer: KtorNioServer,
    readBufferSize: Int,
    writeBufferSize: Int
) : BaseConnection(readBufferSize = readBufferSize, writeBufferSize = writeBufferSize) {

    val isActive get() = !socket.isClosed

    protected val inputChannel by lazy { socket.openReadChannel() }
    protected val outputChannel by lazy { socket.openWriteChannel(autoFlush = true) }

    /**
     * IP address of this Connection.
     */
    override val ip = socket.remoteAddress.toString().replace("/", "")

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
    override fun close(forced: Boolean) {
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
    override fun onlyClose(): Boolean {
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

}