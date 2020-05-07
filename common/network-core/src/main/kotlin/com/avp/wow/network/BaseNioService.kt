package com.avp.wow.network

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import mu.KotlinLogging

abstract class BaseNioService {

    protected val log = KotlinLogging.logger(this::class.java.name)

    protected abstract val scope: CoroutineScope

    /**
     * @return Number of active connections.
     */
    abstract val activeConnectionsCount: Int

    abstract fun connect()

    fun shutdown() {

        log.info { "Stopping NIO server..." }

        closeChannels()

        /**
         * Sending DC packets for active clients
         */
        log.info { "Sending DC packets to clients and close connections..." }
        notifyClose()

        /**
         * Wait 1s for coroutines to execute close operations
         */
        try {
            Thread.sleep(1000)
        } catch (t: Throwable) {
            log.warn(t) { "Nio thread was interrupted during shutdown" }
        }

        log.info { "Active connections: $activeConnectionsCount" }

        /**
         * DC all
         */
        log.info { "Forced Disconnecting all connections..." }
        closeAll()

        /**
         * Wait 1s for coroutines to execute close operations
         */
        try {
            Thread.sleep(1000)
        } catch (t: Throwable) {
            log.warn(t) { "Nio thread was interrupted during shutdown" }
        }

        log.info { "Active connections: $activeConnectionsCount" }

        /**
         * Wait 1s for coroutines to execute close operations
         */
        try {
            Thread.sleep(1000)
        } catch (t: Throwable) {
            log.warn(t) { "Nio thread was interrupted during shutdown" }
        }

        log.info { "NIO server was stopped successfully." }

        scope.cancel("shutdown nio")

    }

    abstract fun closeChannels()

    /**
     * Calls onServerClose method for all active connections.
     */
    protected abstract fun notifyClose()

    /**
     * Close all active connections.
     */
    protected abstract fun closeAll()

}