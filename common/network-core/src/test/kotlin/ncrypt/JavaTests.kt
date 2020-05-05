package ncrypt

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import java.nio.ByteBuffer
import java.nio.ByteOrder

class JavaTests : StringSpec({

    "test" {

        val DATA = 1234567890

        KeyGen.init()

        val blowfishKey = KeyGen.generateBlowfishKey()

        val cryptOne = CryptEngine().apply { updateKey(blowfishKey.encoded) }
        val cryptTwo = CryptEngine().apply { updateKey(blowfishKey.encoded) }

        fun encrypt(buf: ByteBuffer): Int {
            var size = buf.remaining() ///
            val offset = buf.arrayOffset() + buf.position()
            println(buf.array().map { it.toInt() }.joinToString(":"))
            size = cryptOne.encrypt(buf.array(), offset, size)
            println(buf.array().map { it.toInt() }.joinToString(":"))
            return size
        }

        fun decrypt(buf: ByteBuffer): Boolean {
            val size = buf.remaining() //
            val offset = buf.arrayOffset() + buf.position()
            val ret = cryptOne.decrypt(buf.array(), offset, size)
            println(buf.array().map { it.toInt() }.joinToString(":"))
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

                //position(0).limit(remaining())
                println("Before encrypt: ${remaining()}")

                order(ByteOrder.LITTLE_ENDIAN)

                val b2 = slice().limit(remaining()) as ByteBuffer
                val size = encrypt(b2) + 2
                putShort(0, size.toShort())
                position(0).limit(size) as ByteBuffer

                /*val size2 = short// - 2
                println(size2)

                val b = slice().limit(size2.toInt()) as ByteBuffer
                b.order(ByteOrder.LITTLE_ENDIAN)

                //position(position() + size2 - 2)

                decrypt(b)

                println(b.get())
                println(b.int)*/


                /**
                 * Read packet
                 */

                //flip()
                //order(ByteOrder.BIG_ENDIAN)
                //position(0)

                var sz = short
                if (sz > 1) {
                    sz = (sz - 2).toShort()
                }
                val b = slice().limit(sz.toInt()) as ByteBuffer
                b.order(ByteOrder.LITTLE_ENDIAN)

                position(position() + sz)
                //position(2)

                println(sz)

                if (decrypt(b)) {

                    b.get()
                    val result = b.int

                    println(result)

                    //result shouldBe DATA

                } else {

                    println("Wrong checksum from client")

                }

            }!!

    }

})