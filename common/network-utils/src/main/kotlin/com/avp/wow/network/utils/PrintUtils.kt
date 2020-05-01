package com.avp.wow.network.utils

import java.nio.ByteBuffer
import kotlin.experimental.and

object PrintUtils {

    fun hex2bytes(string: String): ByteArray {
        val finalString = string.replace("""\s+""".toRegex(), "")
        val bytes = ByteArray(finalString.length / 2)
        for (i in bytes.indices) {
            bytes[i] = Integer.parseInt(finalString.substring(2 * i, 2 * i + 2), 16).toByte()
        }
        return bytes
    }

    fun bytes2hex(bytes: ByteArray): String {
        val result = StringBuilder()
        for (b in bytes) {
            val value = b and 255.toByte()
            result.append(String.format("%02X", value))
        }
        return result.toString()
    }

    fun toHex(data: ByteBuffer): String {
        val position = data.position()
        val result = StringBuilder()
        var counter = 0
        while (data.hasRemaining()) {
            if (counter % 16 == 0) {
                result.append(String.format("%04X: ", counter))
            }
            val b = data.get() and 255.toByte()
            result.append(String.format("%02X ", b))
            if (++counter % 16 != 0) {
                continue
            }
            result.append("  ")
            toText(data, result, 16)
            result.append("\n")
        }
        val rest = counter % 16
        if (rest > 0) {
            for (i in 0 until 17 - rest) {
                result.append("   ")
            }
            toText(data, result, rest)
        }
        data.position(position)
        return result.toString()
    }

    private fun toText(data: ByteBuffer, result: StringBuilder, cnt: Int) {
        var charPos = data.position() - cnt
        for (a in 0 until cnt) {
            val c = data.get(charPos++)
            if (c in 32..127) {
                result.append(c.toChar())
                continue
            }
            result.append('.')
        }
    }

}