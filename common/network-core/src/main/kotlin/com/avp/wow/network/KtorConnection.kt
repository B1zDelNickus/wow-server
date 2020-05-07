package com.avp.wow.network

import io.ktor.network.sockets.*
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.*
import java.io.IOException

@KtorExperimentalAPI
abstract class KtorConnection(
    val socket: Socket,
    val nio: BaseNioService,
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

}