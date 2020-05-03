package com.avp.wow.network.ktx

import com.avp.wow.network.utils.KtxRunnable
import kotlinx.coroutines.CoroutineScope
import mu.KotlinLogging
import java.nio.channels.SelectableChannel
import java.nio.channels.SelectionKey
import java.nio.channels.spi.SelectorProvider

abstract class Dispatcher(
    private val name: String,
    private val scope: CoroutineScope
) : KtxRunnable {

    protected val log = KotlinLogging.logger(this::class.java.name)

    val selector by lazy { SelectorProvider.provider().openSelector()!! }

    protected val guard = Any()

    abstract fun dispatch()

    fun register(channel: SelectableChannel, ops: Int, acceptor: Acceptor): SelectionKey {
        synchronized(guard) {
            selector.wakeup()
            return channel.register(selector, ops, acceptor)
        }
    }

    fun register(channel: SelectableChannel, ops: Int, connection: KtxConnection) {
        synchronized(guard) {
            selector.wakeup()
            channel.register(selector, ops, connection)
        }
    }

    /**
     * Accept new connection.
     * @param key
     */
    fun accept(key: SelectionKey) {
        try {
            (key.attachment() as Acceptor).accept(key)
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

}