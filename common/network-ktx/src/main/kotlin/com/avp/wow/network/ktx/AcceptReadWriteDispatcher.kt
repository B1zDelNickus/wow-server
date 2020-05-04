package com.avp.wow.network.ktx

import com.avp.wow.network.Dispatcher
import com.avp.wow.network.KtxConnection
import kotlinx.coroutines.CoroutineScope
import java.nio.channels.SelectionKey
import java.util.*

class AcceptReadWriteDispatcher(
    name: String,
    scope: CoroutineScope
) : Dispatcher(
    name = name,
    scope = scope
) {

    /**
     * List of connections that should be closed by this `Dispatcher` as soon as possible.
     */
    private val pendingClose = ArrayList<KtxConnection>()

    override fun dispatch() {

        val selected = selector.select()

        processPendingClose()

        if (selected != 0) {

            selector.selectedKeys().forEach { key ->

                if (key.isValid) {

                    when (key.readyOps()) {

                        SelectionKey.OP_ACCEPT -> accept(key)
                        SelectionKey.OP_READ -> read(key)
                        SelectionKey.OP_WRITE -> write(key)
                        SelectionKey.OP_READ or SelectionKey.OP_WRITE -> key.also { read(it); if (it.isValid) write(it) }

                    }

                }

            }

        }


    }

    /**
     * Add connection to pendingClose list, so this connection will be closed by this `Dispatcher` as soon as possible.
     * @see com.aionemu.commons.network.Dispatcher.closeConnection
     */
    override fun closeConnection(con: KtxConnection) {
        synchronized(pendingClose) { pendingClose.add(con) }
    }

    /**
     * Process Pending Close connections.
     */
    private fun processPendingClose() {
        synchronized(pendingClose) {
            for (connection in pendingClose) {
                closeConnectionImpl(connection)
            }
            pendingClose.clear()
        }
    }

}