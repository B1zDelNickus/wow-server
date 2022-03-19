package com.avp.wow.network

import java.io.IOException
import java.nio.channels.SelectionKey
import java.nio.channels.SocketChannel

abstract class KtxConnection<State: BaseState>(
    private val socketChannel: SocketChannel,
    val dispatcher: Dispatcher,
    readBufferSize: Int,
    writeBufferSize: Int
) : BaseConnection<State>(readBufferSize = readBufferSize, writeBufferSize = writeBufferSize) {

    /**
     * SelectionKey representing this connection.
     */
    var key: SelectionKey? = null

    override val ip: String
        get() = socketChannel.socket().inetAddress.hostAddress

    /**
     * Notify Dispatcher Selector that we want write some data here.
     */
    protected fun enableWriteInterest() {
        key?.let { k ->
            if (k.isValid) {
                k.interestOps(k.interestOps() or SelectionKey.OP_WRITE)
                k.selector().wakeup()
            }
        } ?: throw IllegalStateException("Selection key must be not null!")

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
            dispatcher.closeConnection(this)
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
                if (socketChannel.isOpen) {
                    socketChannel.close()
                    key?.attach(null)
                    key?.cancel()
                }
                closed = true
            } catch (ignored: IOException) {
            }
        }
        return true
    }

}