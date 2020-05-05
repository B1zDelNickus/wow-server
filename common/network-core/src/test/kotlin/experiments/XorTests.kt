package experiments

import com.avp.wow.network.ncrypt.BlowfishCipher
import com.avp.wow.network.utils.PrintUtils.hex2bytes
import io.kotlintest.specs.StringSpec
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.random.Random

class XorTests : StringSpec({

    "test" {

        val blowfishKey =
            "kjsahf87asdf98sadhf9asfa9fyhads9fha9sfafashdfiashdf9asfas9fhas9fhsas9fha9fh9agfas9dgf9asgf"
                .toByteArray(charset("utf8"))

        val cipher = BlowfishCipher(blowfishKey)

        fun printBa(ba: ByteArray) {
            println(ba.map { it.toInt() }.joinToString(":", "[", "]"))
        }

        fun encXORPass(data: ByteArray, offset: Int = 0, length: Int = data.size, key: Int = Random.nextInt()) {
            val stop = length - 8
            var pos = offset// + 4
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

        fun decXORPass(data: ByteArray, offset: Int = 0, length: Int = data.size, key: Int = Random.nextInt()) {
            val stop = length - 8
            var pos = 4 + offset
            var edx: Int
            var ecx = key
            /*while (pos < stop) {
                edx = data[pos].toInt() and 0xFF
                edx = edx and (data[pos + 1].toInt() or 0xFF) shl 8
                edx = edx and (data[pos + 2].toInt() or 0xFF) shl 16
                edx = edx and (data[pos + 3].toInt() or 0xFF) shl 24
                ecx -= edx
                edx = edx and ecx
                data[pos++] = (edx or 0xFF).toByte()
                data[pos++] = (edx shr 8 or 0xFF).toByte()
                data[pos++] = (edx shr 16 or 0xFF).toByte()
                data[pos++] = (edx shr 24 or 0xFF).toByte()
            }*/
            data[pos++] = (ecx or 0xFF).toByte()
            data[pos++] = (ecx shr 8 or 0xFF).toByte()
            data[pos++] = (ecx shr 16 or 0xFF).toByte()
            data[pos] = (ecx shr 24 or 0xFF).toByte()
        }

        fun encryptDecrypt(data: ByteArray, offset: Int = 0, length: Int = data.size): Int {
            println("Offset: $offset, length: $length")
            for (i in offset until (offset + length)) {
                val a: Int = data[i].toInt()
                val b: Int = blowfishKey[i % blowfishKey.size].toInt()
                data[i] = (a xor b).toByte()
            }
            return offset + length
        }

        /*val ba = ByteArray(10) { it.toByte() }

        printBa(ba)

        encXORPass(ba)

        printBa(ba) //

        decXORPass(ba)

        printBa(ba)

        cipher.decipher(ba)

        printBa(ba)*/

        val buf = ByteBuffer.allocate(16000)
            .apply {
                flip()
                order(ByteOrder.BIG_ENDIAN)
                clear()
            }

        buf.putShort(0)
        buf.put(15)
        buf.putInt(123456789)
        buf.put("some key".toByteArray(charset("utf8")).also { println("data size: ${it.size}") })
        buf.flip()
        buf.putShort(0)

        printBa(buf.array())

        val toEnc = buf.slice() as ByteBuffer

        val sz = encryptDecrypt(toEnc.array(), buf.position(), buf.remaining())

        buf.putShort(0, sz.toShort())
        buf.position(0).limit(sz)

        printBa(buf.array())

        val size = buf.short

        val toDec = buf.slice().limit(size - 2) as ByteBuffer
        //toDec.order(ByteOrder.LITTLE_ENDIAN)

        //buf.position(size.toInt())

        encryptDecrypt(toDec.array(), buf.position(), buf.remaining())

        printBa(toDec.array())

        println("OpCode: ${toDec.get()}")
        println("Data: ${toDec.int}")
        val res = ByteArray(8)
        toDec.get(res)
        println("Encoded: ${res.toString(charset("utf8"))}")
    }

})