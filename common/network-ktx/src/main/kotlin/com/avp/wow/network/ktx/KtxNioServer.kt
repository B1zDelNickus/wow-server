package com.avp.wow.network.ktx

import com.avp.wow.network.BaseNioServer
import kotlinx.coroutines.*
import java.net.InetSocketAddress
import java.nio.channels.SelectionKey
import java.nio.channels.ServerSocketChannel
import kotlin.coroutines.CoroutineContext

class KtxNioServer(
    private val serverConfigs: List<KtxServerConfig> = emptyList(),
    private val readWriteThreads: Int = 0,
    context: CoroutineContext = Dispatchers.IO
) : BaseNioServer() {

    override val scope by lazy {
        CoroutineScope(SupervisorJob() + context)
    }
    override val getActiveConnections: Int
        get() = if (readWriteDispatchers != null) {
            var count = 0
            for (d in readWriteDispatchers!!) {
                count = d.selector.keys().size
            }
            count
        } else {
            acceptDispatcher.selector.keys().size - serverChannelKeys.size
        }

    private lateinit var acceptDispatcher: Dispatcher
    private var readWriteDispatchers: Array<Dispatcher>? = null
    private var currentReadWriteDispatcher = 0

    private val serverChannelKeys = arrayListOf<SelectionKey>()

    @Throws(Error::class)
    override fun connect() {

        log.info { "Starting NIO server..." }

        try {

            initDispatchers()

            val serverChannel = ServerSocketChannel.open()
                .apply { configureBlocking(false) }

            serverConfigs.forEach { cfg ->

                val isa = when (cfg.hostName) {
                    "*" -> {
                        log.info { "Server listening on all available IPs on Port " + cfg.port.toString() + " for " + cfg.connectionName }
                        InetSocketAddress(cfg.port)
                    }
                    else -> {
                        log.info { "Server listening on IP: " + cfg.hostName + " Port " + cfg.port + " for " + cfg.connectionName }
                        InetSocketAddress(cfg.hostName, cfg.port)
                    }
                }

                serverChannel.bind(isa)

                acceptDispatcher.register(
                    serverChannel,
                    SelectionKey.OP_ACCEPT,
                    Acceptor(cfg.factory, this)
                ).also { key -> serverChannelKeys += key }

            }

        } catch (e: Exception) {

            log.error(e) { "Error while NIO server initialization occurred." }
            throw Error(e)

        }

        log.info { "NIO server was started successfully." }

    }

    private fun initDispatchers() {

        log.info { "Starting accept/read/write dispatchers..." }

        if (readWriteThreads < 1) {

            acceptDispatcher =
                AcceptReadWriteDispatcher(name = "AcceptReadWrite Dispatcher", scope = scope)
            scope.launch { acceptDispatcher.run() }

        } else {

            acceptDispatcher = AcceptDispatcher(name = "Accept Dispatcher", scope = scope)
            scope.launch { acceptDispatcher.run() }

            readWriteDispatchers = Array(readWriteThreads) { no ->
                AcceptReadWriteDispatcher(name = "ReadWrite-$no Dispatcher", scope = scope)
                    .also { rwd ->
                        scope.launch {
                            rwd.run()
                        }
                    }
            }

        }

        log.info { "Dispatchers were successfully started." }

    }

    fun getReadWriteDispatcher(): Dispatcher {
        if (readWriteDispatchers == null) {
            return acceptDispatcher
        }
        if (readWriteDispatchers!!.size == 1) {
            return readWriteDispatchers!![0]
        }
        if (currentReadWriteDispatcher >= readWriteDispatchers!!.size) {
            currentReadWriteDispatcher = 0
        }
        return readWriteDispatchers!![currentReadWriteDispatcher++]
    }

    override fun closeChannels() {
        try {
            for (key in serverChannelKeys) {
                key.cancel()
            }
        } catch (e: Exception) {
            log.error(e) { "Error while stopping NIO server occurred." }
        }
    }

    override fun notifyServerClose() {
        TODO("Not yet implemented")
    }

    override fun closeAll() {
        TODO("Not yet implemented")
    }

}