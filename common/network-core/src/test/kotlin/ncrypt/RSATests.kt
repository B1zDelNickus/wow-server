package ncrypt

import com.avp.wow.network.ncrypt.KeyGen
import io.kotlintest.specs.StringSpec
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec


class RSATests : StringSpec({

    "test" {

        KeyGen.init()

        val encryptedRSAKeyPair = KeyGen.encryptedRSAKeyPair

        val encryptedModulus = encryptedRSAKeyPair.encryptedModulus
        val rsaPublicKey = encryptedRSAKeyPair.rsaKeyPair.public.encoded

        val rsaPrivateKey = encryptedRSAKeyPair.rsaKeyPair.private

        val pubKey = KeyFactory.getInstance("RSA")
            //.generatePrivate(RSAPrivateKeySpec(BigInteger(encryptedModulus), BigInteger("65537", 10)))
            .generatePublic(X509EncodedKeySpec(rsaPublicKey))!!
            //.generatePublic(RSAPublicKeySpec(BigInteger(encryptedModulus), BigInteger("65537", 10)))

        val raw = "admin admin".toByteArray()

        val encCipher = Cipher.getInstance("RSA/ECB/NoPadding")!!
        encCipher.init(Cipher.ENCRYPT_MODE, pubKey)
        val cipherBytes = encCipher.doFinal(raw)!!

        println("enc size: ${cipherBytes.size}")

        val cipherText: String = Base64.getEncoder().encodeToString(cipherBytes)
        val data = Base64.getDecoder().decode(cipherText)


        val decCipher = Cipher.getInstance("RSA/ECB/NoPadding")!!
        decCipher.init(Cipher.DECRYPT_MODE, rsaPrivateKey)
        val decrypted = decCipher.doFinal(data)!!//, 0, 64)!!

        val resultString = decrypted
            .filter { it.toInt() != 0 }
            .toByteArray()
            .toString(Charsets.UTF_8)
            .trim()
            .split(" ")

        println(resultString)

    }

    "f:aes test" {

        val data = "admin admin".toByteArray()

        val key = "1234567812345678".toByteArray()
        val secret = SecretKeySpec(key, "AES")
        val encCipher = Cipher.getInstance("AES")
        encCipher.init(Cipher.ENCRYPT_MODE, secret)
        val encoded = encCipher.doFinal(data)!!

        val decCipher = Cipher.getInstance("AES")
        decCipher.init(Cipher.DECRYPT_MODE, secret)
        val decoded = decCipher.doFinal(encoded)!!

        println(data.size)
        println(encoded.size)
        println(decoded.size)

        println(String(data))
        println(String(encoded))
        println(String(decoded))

    }

})