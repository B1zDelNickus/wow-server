package com.avp.wow.network

import com.avp.wow.network.ncrypt.BlowfishCipher
import com.avp.wow.network.ncrypt.CryptEngine
import com.avp.wow.network.ncrypt.KeyGen
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import mu.KotlinLogging
import java.nio.ByteBuffer
import java.nio.ByteOrder

class NcryptTests : StringSpec({

    val log = KotlinLogging.logger("tester")

    "blowfish test" {

        val DATA = 1234567890

        KeyGen.init()

        val blowfishKey = KeyGen.generateBlowfishKey()

        val cryptOne = CryptEngine().apply { /*updateKey(blowfishKey.encoded)*/ }
        val cryptTwo = CryptEngine().apply { /*updateKey(blowfishKey.encoded)*/ }

        fun encrypt(buf: ByteBuffer): Int {
            var size = buf.limit() - 2
            val offset = buf.arrayOffset() + buf.position()
            size = cryptOne.encrypt(buf.array(), offset, size)
            return size
        }

        fun decrypt(buf: ByteBuffer): Boolean {
            val size = buf.remaining()
            val offset = buf.arrayOffset() + buf.position()
            val ret = cryptOne.decrypt(buf.array(), offset, size)
            return ret
        }

        val buffer = ByteBuffer.allocate(15000 * 2)
            .apply {
                flip()
                order(ByteOrder.BIG_ENDIAN)

                clear()

                /**
                 * Write packet
                 */
                putShort(0.toShort())
                put(15.toByte())
                putInt(DATA)
                flip()
                putShort(0.toShort())
                val b2 = slice()
                val size = limit() // encrypt(b2) + 2
                putShort(0, size.toShort())
                val input = position(0).limit(size) as ByteBuffer


                /**
                 * Read packet
                 */

                //flip()

                var sz = input.short
                if (sz > 1) {
                    sz = (sz - 2).toShort()
                }
                val b = input.slice().limit(sz.toInt()) as ByteBuffer
                //b.order(ByteOrder.LITTLE_ENDIAN)
                /**
                 * read message fully
                 */
                input.position(input.position() + sz)

                println(b.get())
                val result = b.int

                result shouldBe DATA

                /*if (decrypt(b)) {

                    b.get()
                    val result = b.int

                    result shouldBe DATA

                } else {

                    log.warn { "Wrong checksum from client" }

                }*/

            }!!

    }

    "!:buffer experiments" {

        val buffer = ByteBuffer.allocate(8192 * 2)
            .apply {
                order(ByteOrder.BIG_ENDIAN)
                flip()
            }

        buffer.clear()

        buffer.putShort(0.toShort())
        buffer.put(9.toByte())
        buffer.putInt(123)
        buffer.flip()
        buffer.putShort(0.toShort())

        val size = buffer.limit()

        buffer.putShort(0, size.toShort())

        //buffer.order(ByteOrder.LITTLE_ENDIAN)

        buffer.position(0).limit(size)

        val size2 = buffer.short - 2

        val b = buffer.slice().limit(size2) as ByteBuffer

        println(b.remaining())
        //println(buffer.short)
        println(b.get())
        println(b.int)

    }

    "f:blowfish cipher test" {

        //KeyGen.init()

        //val blowfishKey = KeyGen.generateBlowfishKey()
        val blowfishKey = byteArrayOf(
            0x6b.toByte(),
            0x60.toByte(),
            0xcb.toByte(),
            0x5b.toByte(),
            0x82.toByte(),
            0xce.toByte(),
            0x90.toByte(),
            0xb1.toByte(),
            0xcc.toByte(),
            0x2b.toByte(),
            0x6c.toByte(),
            0x55.toByte(),
            0x6c.toByte(),
            0x6c.toByte(),
            0x6c.toByte(),
            0x6c.toByte()
        )

        val cipher = BlowfishCipher(blowfishKey/*.encoded*/)

        fun printBA(ba: ByteArray) {
            println(ba.map { it.toInt() }.joinToString(":", "[", "]"))
        }

        val ba = ByteArray(10) { it.toByte() }

        printBA(ba)

        cipher.cipher(ba)

        printBA(ba)

        cipher.decipher(ba)

        printBA(ba)

    }

})