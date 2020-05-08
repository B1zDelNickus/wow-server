package ncrypt

import com.avp.wow.network.ncrypt.KeyGen
import io.kotlintest.specs.StringSpec
import java.nio.charset.Charset
import java.security.GeneralSecurityException
import java.util.*
import javax.crypto.Cipher

class PubicKeyTests : StringSpec({

    "test" {

        KeyGen.init()

        val rsa = KeyGen.encryptedRSAKeyPair

        val data = "admin admin".toByteArray()

        /*val data = ByteArray(32)
            .also { buf ->
                "admin".toByteArray()
                    .forEachIndexed { index, byte ->
                        buf[index] = byte
                    }

            } + ByteArray(32)
            .also { buf ->
                "admin".toByteArray()
                    .forEachIndexed { index, byte ->
                        buf[index] = byte
                    }
            }*/

        val decrypted = try {
            val rsaCipher = Cipher.getInstance("RSA/ECB/NoPadding")!!

            rsaCipher.init(Cipher.ENCRYPT_MODE, rsa.rsaKeyPair.public)
            val cipherBytes = rsaCipher.doFinal(data)!!

            println("cipher: ${cipherBytes.toString(Charset.defaultCharset())}")
            println("cipher: ${cipherBytes.size}")
            val returnValue = cipherBytes.toString(Charset.defaultCharset())

            val cipherText: String = Base64.getEncoder().encodeToString(cipherBytes)
            val reCipherBytes: ByteArray = Base64.getDecoder().decode(cipherText)

            println("reCipher: ${reCipherBytes.toString(Charset.defaultCharset())}")
            println("reCipher: ${reCipherBytes.size}")

            rsaCipher.init(Cipher.DECRYPT_MODE, rsa.rsaKeyPair.private)
            val result = rsaCipher.doFinal(reCipherBytes, 0, 128)!!

            println("result: ${result.toString(Charset.defaultCharset())}")

            result.toString(Charset.defaultCharset()).split(" ")
                .map { it.trim { c -> c == ' ' } }
                .also {
                    println("user: ${it[0]}, pass: ${it[1]}")
                }


        } catch (e: GeneralSecurityException) {
            //sendPacket(SM_LOGIN_FAIL(AionAuthResponse.SYSTEM_ERROR))

        }

    }

})