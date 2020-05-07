package com.avp.wow.network

import mu.KotlinLogging
import java.nio.ByteBuffer
import java.nio.ByteOrder

abstract class BaseConnection(
    readBufferSize: Int,
    writeBufferSize: Int
) {

    protected val log = KotlinLogging.logger(this::class.java.name)

    val readBuffer by lazy {
        ByteBuffer.allocate(readBufferSize)
            .apply {
                order(ByteOrder.BIG_ENDIAN)
            }!!
    }

    val writeBuffer by lazy {
        ByteBuffer.allocate(writeBufferSize)
            .apply {
                flip()
                order(ByteOrder.BIG_ENDIAN)
            }!!
    }

    /**
     * IP address of this Connection.
     */
    abstract val ip: String

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
     * Object on witch some methods are synchronized
     */
    protected val guard = Any()

    /**
     * Connection will be closed at some time [by Dispatcher Thread], after that onDisconnect() method will be called to clear all other things.
     * @param forced is just hint that getDisconnectionDelay() should return 0 so OnDisconnect() method will be called without any delay.
     */
    abstract fun close(forced: Boolean = false)

    /**
     * This will only close the connection without taking care of the rest. May be called only by Dispatcher Thread. Returns true if connection was not closed before.
     * @return true if connection was not closed before.
     */
    abstract fun onlyClose(): Boolean

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
    abstract fun processData(data: ByteBuffer): Boolean

    /**
     * This method will be called by Dispatcher, and will be repeated till return false.
     * @param data
     * @return True if data was written to buffer, False indicating that there are not any more data to write.
     */
    abstract fun writeData(data: ByteBuffer): Boolean

    /**
     * Called when AConnection object is fully initialized and ready to process and send packets. It may be used as hook for sending first packet etc.
     */
    abstract fun initialized()

    /**
     * This method is called by Dispatcher when connection is ready to be closed.
     * @return time in ms after witch onDisconnect() method will be called.
     */
    abstract val disconnectionDelay: Long

    /**
     * This method is called by Dispatcher to inform that this connection was closed and should be cleared. This method is called only once.
     */
    abstract fun onDisconnect()

    /**
     * This method is called by NioServer to inform that NioServer is shouting down. This method is called only once.
     */
    abstract fun onServerClose()

    abstract fun enableEncryption(blowfishKey: ByteArray)

}