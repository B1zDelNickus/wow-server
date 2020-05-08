package ncrypt

import com.avp.wow.network.ncrypt.BlowfishCipher
import com.avp.wow.network.ncrypt.KeyGen
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import ncrypt.WowCryptEngine.Companion.STATIC_SERVER_PACKET_CODE
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.experimental.inv

class WowCryptTest : StringSpec({

    "f:encode" {

        val code1 = 15
        val code2 = 61
        val mask1 = 20
        val mask2 = 30

        println((code1 + mask1) xor mask2)
        println(code1 xor mask1)
        println(27 xor mask1)
        println(61 xor mask2 - mask1) // 123123
        println((code2 xor mask2) - mask1) // 123123

    }

    "test" {

        fun printBuf(buf: ByteBuffer) {
            println(
                buf.array()
                    .map { it.toInt() }
                    .joinToString(":", "[", "]")
            )
        }

        KeyGen.init()

        val blowfishKey = KeyGen.generateBlowfishKey().encoded!!

        val serverEngine = WowCryptEngine()
            .apply { updateKey(newKey = blowfishKey) }

        val clientEngine = WowCryptEngine()

        val packet = ByteBuffer.allocate(8196 * 2)
            .apply {
                flip()
                order(ByteOrder.BIG_ENDIAN)
                clear()
            }!!

        packet
            .apply {

                putShort(0)

                val oc = WowCryptEngine.encodeOpCodec(0x09)
                putShort(oc.toShort()) //
                put(WowCryptEngine.STATIC_SERVER_PACKET_CODE)
                putShort(oc.toShort().inv())
                // putShort(oc.toShort())

                putLong(123456789)
                put(blowfishKey)

                flip()

                putShort(limit().toShort())

                val b = slice()
                serverEngine.encrypt(data = b)

                position(0)

            }

        packet
            .apply {

                //order(ByteOrder.BIG_ENDIAN)

                val size = short - 2

                println("Received packet with size: $size")

                val b = slice().limit(size) as ByteBuffer

                if (clientEngine.decrypt(data = b)) {

                    short
                    get()
                    short

                    long shouldBe 123456789
                    val arr = ByteArray(16)
                    get(arr)
                    arr shouldBe blowfishKey

                    clientEngine.updateKey(newKey = arr)

                } else {
                    println("Not encrypted: ${getShort(2)} : ${getShort(5).inv()} : ${get(4)} : ${STATIC_SERVER_PACKET_CODE}")
                    printBuf(b)
                }

                clear()

            }

        packet
            .apply {

                putShort(0)

                val oc = WowCryptEngine.encodeOpCodec(0x07)
                putShort(oc.toShort()) //
                put(WowCryptEngine.STATIC_SERVER_PACKET_CODE)
                putShort(oc.toShort().inv())
                // putShort(oc.toShort())

                putLong(123456789)
                putInt(1)
                putInt(2)
                putInt(3)

                flip()

                putShort(limit().toShort())

                printBuf(this)

                val b = slice()
                serverEngine.encrypt(data = b)

                printBuf(b)

                position(0)

            }

        packet
            .apply {

                //order(ByteOrder.BIG_ENDIAN)

                val size = short - 2

                println("Received packet with size: $size")

                val b = slice().limit(size) as ByteBuffer

                if (clientEngine.decrypt(data = b)) {

                    printBuf(b)

                    short
                    get()
                    short

                    long shouldBe 123456789
                    int shouldBe 1
                    int shouldBe 2
                    int shouldBe 3

                } else {
                    println("Not encrypted: ${getShort(2)} : ${getShort(5).inv()} : ${get(4)} : ${STATIC_SERVER_PACKET_CODE}")
                    printBuf(b)
                }

                clear()

            }

    }

})

class WowCryptEngine {

    private var key = INITIAL_KEY

    /**
     * Tells you whether the key is updated or not
     */
    private var updatedKey = false

    /**
     * A secret blowfish cipher
     */
    private val cipher = BlowfishCipher(blowfishKey = key)

    /**
     * Update the key for packet encryption/decryption with the Blowfish Cipher
     * @param newKey new Blowfish Key
     */
    fun updateKey(newKey: ByteArray, force: Boolean = false) {
        key = newKey
        if (force) updatedKey = true
    }

    fun decrypt(data: ByteBuffer, offset: Int = data.arrayOffset() + data.position(), length: Int = data.remaining()): Boolean {
        val arr = data.array()
        cipher.decipher(arr, offset, length)
        xorEncryptDecrypt(arr, offset, length)
        /**
         * Initial packet with key must probably
         * Key will be updated after packet parsing
         */
        /*if (!updatedKey) {
            return true
        }*/
        return validateClientPacket(buf = data)
    }

    fun encrypt(data: ByteBuffer, offset: Int = data.arrayOffset() + data.position(), length: Int = data.remaining()) {
        val arr = data.array()
        xorEncryptDecrypt(arr, offset, length)
        cipher.cipher(arr, offset, length)
        /*if (!updatedKey) {
            updatedKey = true
        }*/
    }

    private fun xorEncryptDecrypt(data: ByteArray, offset: Int = 0, length: Int = data.size) {
        for (i in offset until (offset + length + 2)) {
            val a: Int = data[i].toInt()
            val b: Int = STATIC_XOR_KEY[i % STATIC_XOR_KEY.size].toInt()
            data[i] = (a xor b).toByte()
        }
    }

    private fun validateClientPacket(buf: ByteBuffer): Boolean {
        return (buf.getShort(0) == buf.getShort(3).inv()) && buf[2] == STATIC_SERVER_PACKET_CODE
    }

    companion object {

        private val INITIAL_KEY = byteArrayOf(
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

        private val STATIC_XOR_KEY =
            "nKO/WctQ0AVLbpzfBkS6NevDYT8ourG5CRlmdjyJ72aswx4EPq1UgZhFMXH?3iI9"
                .toByteArray()

        const val STATIC_SERVER_PACKET_CODE: Byte = 0x56

        private const val MASK_1: Int = 0xD4
        private const val MASK_2: Int = 0xD5

        /**
         * Server packet opcodec obfuscation.
         * @param op
         * @return obfuscated opcodec
         */
        fun encodeOpCodec(op: Int) = op + MASK_1 xor MASK_2

        fun decodeOpCodec(op: Int) = op and MASK_2 - MASK_1

    }

}