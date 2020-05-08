package com.avp.wow.network.utils

import java.nio.ByteBuffer

/**
 * @author -Nemesiss-
 */
object Util {

    fun printSection(sectionName: String) {
        var sectionName = sectionName
        sectionName = "=[ $sectionName ]"
        while (sectionName.length < 79) {
            sectionName = "-$sectionName"
        }
        println(sectionName)
    }

    fun printProgressBarHeader(size: Int) {
        val header = StringBuilder("0%[")
        for (i in 0 until size) {
            header.append("-")
        }
        header.append("]100%")
        println(header)
        print("   ")
    }

    fun printCurrentProgress() {
        print("+")
    }

    fun printEndProgress() {
        print("+ Done\n")
    }

    /**
     * Convert data from given ByteBuffer to hex
     * @param data
     * @return hex
     */
    fun toHex(data: ByteBuffer): String {
        val result = StringBuilder()
        var counter = 0
        var b: Int
        while (data.hasRemaining()) {
            if (counter % 16 == 0) {
                result.append(String.format("%04X: ", counter))
            }
            b = data.get().toInt() and 0xff
            result.append(String.format("%02X ", b))
            counter++
            if (counter % 16 == 0) {
                result.append("  ")
                toText(data, result, 16)
                result.append("\n")
            }
        }
        val rest = counter % 16
        if (rest > 0) {
            for (i in 0 until 17 - rest) {
                result.append("   ")
            }
            toText(data, result, rest)
        }
        return result.toString()
    }

    /**
     * Gets last <tt>cnt</tt> read bytes from the <tt>data</tt> buffer and puts into <tt>result</tt> buffer in special format:
     *
     *  * if byte represents char from partition 0x1F to 0x80 (which are normal ascii chars) then it's put into buffer as it is
     *  * otherwise dot is put into buffer
     *
     * @param data
     * @param result
     * @param cnt
     */
    private fun toText(data: ByteBuffer, result: StringBuilder, cnt: Int) {
        var charPos = data.position() - cnt
        for (a in 0 until cnt) {
            val c = data[charPos++].toInt()
            if (c in 32..127) {
                result.append(c.toChar())
            } else {
                result.append('.')
            }
        }
    }

    /**
     * Converts name to valid pattern For example : "atracer" -> "Atracer"
     * @param name
     * @return String
     */
    fun convertName(name: String): String {
        return if (name.isNotEmpty()) {
            name.substring(0, 1).toUpperCase() + name.toLowerCase().substring(1)
        } else ""
    }
}