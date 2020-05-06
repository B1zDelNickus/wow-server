package ncrypt

/*
import io.kotlintest.specs.StringSpec
import java.nio.ByteBuffer
import java.nio.ByteOrder

class CryptEngineTests : StringSpec({

    "test" {

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

        val crypt = CryptEngine()

        val cipher = BlowfishCipher(blowfishKey)

        val data = 1234567890

        fun encXORPass(data: ByteArray, offset: Int, length: Int, key: Int) {
            val stop = length - 8
            var pos = 4 + offset
            var edx: Int
            var ecx = key
            while (pos < stop) {
                edx = data[pos].toInt() and 0xFF
                edx = edx or (data[pos + 1].toInt() and 0xFF) shl 8
                edx = edx or (data[pos + 2].toInt() and 0xFF) shl 16
                edx = edx or (data[pos + 3].toInt() and 0xFF) shl 24
                ecx += edx
                edx = edx xor ecx
                data[pos++] = (edx and 0xFF).toByte()
                data[pos++] = (edx shr 8 and 0xFF).toByte()
                data[pos++] = (edx shr 16 and 0xFF).toByte()
                data[pos++] = (edx shr 24 and 0xFF).toByte()
            }
            data[pos++] = (ecx and 0xFF).toByte()
            data[pos++] = (ecx shr 8 and 0xFF).toByte()
            data[pos++] = (ecx shr 16 and 0xFF).toByte()
            data[pos] = (ecx shr 24 and 0xFF).toByte()
        }

        fun decXORPass(data: ByteArray, offset: Int, length: Int, key: Int) {

        }

        */
/*fun xor(data: ByteArray): List<Byte> {
            var key = 0x99.toByte()
            return data.map { b -> b xor key }
        }*//*


        fun encryptDecrypt(data: ByteArray) {
            val key = blowfishKey
            val result = arrayListOf<Byte>()
            //println(data.size)
            //println(data.indices)
            for (i in data.indices) {
                val a: Int = data[i].toInt()
                val b: Int = key[i % key.size].toInt()
                data[i] = (a xor b).toByte()
                //result.add()
            }

            //return result.toByteArray()
        }

        fun encrypt(b: ByteBuffer) : Int {

            */
/*var length = size + 4
            length += 4
            length += 8 - length % 8

            println(b.array().map { it.toInt() }.joinToString(":", "[", "]"))

            encXORPass(b.array(), offset, length, 4)
            //val encrypted = xor(b.array())
            //println(encrypted.map { it.toInt() }.joinToString(":", "[", "]"))

            println(b.array().map { it.toInt() }.joinToString(":", "[", "]"))

            cipher.cipher(b.array(), offset, size)

            println(b.array().map { it.toInt() }.joinToString(":", "[", "]"))*//*


            println(b.array().take(b.remaining() + 2).map { it.toInt() }.joinToString(":", "[", "]"))

            encryptDecrypt(b.array().toList().take(b.remaining() + 2).toByteArray())

            println(b.array().take(b.remaining() + 2).map { it.toInt() }.joinToString(":", "[", "]"))

            //encryptDecrypt(b.array().toList().take(b.remaining() + 2).toByteArray())

            //println(b.array().take(b.remaining() + 2).map { it.toInt() }.joinToString(":", "[", "]"))

            val size = b.remaining()
            val offset = b.arrayOffset() + b.position()

            println(b.array().map { it.toInt() }.joinToString(":", "[", "]"))

            cipher.cipher(b.array(), offset, size)

            println(b.array().map { it.toInt() }.joinToString(":", "[", "]"))

            */
/*println(b.array().map { it.toInt() }.joinToString(":", "[", "]"))

            val len = crypt.encrypt(b.array(), offset, size - 2)
            //cipher.cipher(b.array(), offset, size)

            println(b.array().map { it.toInt() }.joinToString(":", "[", "]"))*//*


            return b.limit() + 2 // len + 2 //
        }

        fun decrypt(b: ByteBuffer) {
            val size = b.remaining()
            val offset = b.arrayOffset() + b.position()

            */
/*var length = size + 4
            length += 4
            length += 8 - length % 8*//*


            */
/*println(b.array().map { it.toInt() }.joinToString(":", "[", "]"))

            encXORPass(b.array(), offset, size, 4)

            println(b.array().map { it.toInt() }.joinToString(":", "[", "]"))

            cipher.decipher(b.array(), offset, size)

            println(b.array().map { it.toInt() }.joinToString(":", "[", "]"))*//*


            println(b.array().map { it.toInt() }.joinToString(":", "[", "]"))

            //crypt.decrypt(b.array(), offset, size)
            cipher.decipher(b.array(), offset, size)

            println(b.array().map { it.toInt() }.joinToString(":", "[", "]"))

        }

        val buf = ByteBuffer.allocate(16_000)
            .apply {
                flip()
                order(ByteOrder.BIG_ENDIAN)
                clear()
            }!!

        buf.putShort(0)
        buf.put(1)
        buf.putInt(data)
        buf.flip()
        buf.putShort(0)

        val toEncrypt = buf.slice() as ByteBuffer
        val size = encrypt(toEncrypt)

        buf.putShort(0, size.toShort())
        buf.position(0).limit(size)

        val readSize = buf.short

        println("Size: $readSize")

        val toDecrypt = buf.slice().limit(readSize - 2) as ByteBuffer
        toDecrypt.order(ByteOrder.LITTLE_ENDIAN)
        buf.position(readSize.toInt())

        decrypt(toDecrypt)

        //toDecrypt.order(ByteOrder.BIG_ENDIAN)

        println(toDecrypt.get())
        println(toDecrypt.int)


    }

})*/
