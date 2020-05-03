package com.avp.wow.network.ktx.packet

import com.avp.wow.network.ktx.KtxConnection
import com.avp.wow.network.packet.BasePacket
import com.avp.wow.network.utils.KtxRunnable
import java.nio.ByteBuffer

abstract class BaseClientPacket<T : KtxConnection> : BasePacket, KtxRunnable {

    constructor(buffer: ByteBuffer, opCode: Int) : super(opCode = opCode, type = Companion.PacketType.CLIENT) {
        this.buffer = buffer
    }

    constructor(opCode: Int) : super(opCode = opCode, type = Companion.PacketType.CLIENT)

    constructor(buffer: ByteBuffer) : super(type = Companion.PacketType.CLIENT) {
        this.buffer = buffer
    }

    var connection: T? = null

    /**
     * This method reads data from a packet buffer. If the error occurred while reading data, the connection is closed.
     * @return `true` if reading was successful, otherwise `false`
     */
    fun read(): Boolean {
        return try {
            readImpl()
            if (getRemainingBytes > 0) {
                log.debug("Packet $this not fully readed!")
            }
            true
        } catch (re: Exception) {
            log.error("Reading failed for packet $this", re)
            false
        }
    }

    /**
     * Data reading implementation
     */
    protected abstract fun readImpl()

    /**
     * @return number of bytes remaining in this packet buffer.
     */
    val getRemainingBytes get() = buffer?.remaining() ?: 0

    /**
     * Read int from this packet buffer.
     * @return int
     */
    protected fun readD(): Int {
        try {
            return buffer!!.int
        } catch (e: Exception) {
            log.error("Missing D for: $this")
        }
        return 0
    }

    /**
     * Read byte from this packet buffer.
     * @return int
     */
    protected fun readC(): Int {
        try {
            return buffer!!.get().toInt() and 0xFF
        } catch (e: Exception) {
            log.error("Missing C for: $this")
        }
        return 0
    }

    /**
     * Read signed byte from this packet buffer.
     * @return int
     */
    protected fun readSC(): Byte {
        try {
            return buffer!!.get()
        } catch (e: Exception) {
            log.error("Missing C for: $this")
        }
        return 0
    }

    /**
     * Read signed short from this packet buffer.
     * @return int
     */
    protected fun readSH(): Short {
        try {
            return buffer!!.short
        } catch (e: Exception) {
            log.error("Missing H for: $this")
        }
        return 0
    }

    protected fun readH(): Int {
        try {
            return buffer!!.short.toInt() and 0xFFFF
        } catch (e: Exception) {
            log.error("Missing H for: $this")
        }
        return 0
    }

    /**
     * Read double from this packet buffer.
     * @return double
     */
    protected fun readDF(): Double {
        try {
            return buffer!!.double
        } catch (e: Exception) {
            log.error("Missing DF for: $this")
        }
        return 0.0
    }

    /**
     * Read double from this packet buffer.
     * @return double
     */
    protected fun readF(): Float {
        try {
            return buffer!!.float
        } catch (e: Exception) {
            log.error("Missing F for: $this")
        }
        return 0f
    }

    /**
     * Read long from this packet buffer.
     * @return long
     */
    protected fun readQ(): Long {
        try {
            return buffer!!.long
        } catch (e: Exception) {
            log.error("Missing Q for: $this")
        }
        return 0
    }

    /**
     * Read String from this packet buffer.
     * @return String
     */
    protected fun readS(): String {
        val sb = StringBuffer()
        var ch: Char
        try {
            while (buffer!!.char.also { ch = it }.toInt() != 0) {
                sb.append(ch)
            }
        } catch (e: Exception) {
            log.error("Missing S for: $this")
        }
        return sb.toString()
    }

    /**
     * Read n bytes from this packet buffer, n = length.
     * @param length
     * @return byte[]
     */
    protected fun readB(length: Int): ByteArray {
        val result = ByteArray(length)
        try {
            buffer!!.get(result)
        } catch (e: Exception) {
            log.error("Missing byte[] for: $this")
        }
        return result
    }

    /**
     * Execute this packet action.
     */
    protected abstract suspend fun runImpl()

}