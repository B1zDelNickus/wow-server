package com.avp.wow.network.todo_move

import mu.KotlinLogging
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import java.security.GeneralSecurityException
import java.security.KeyPairGenerator
import java.security.spec.RSAKeyGenParameterSpec
import kotlin.random.Random

/**
 * Key generator. It generates keys or keyPairs for Blowfish and RSA
 * @author -Nemesiss-
 */
object KeyGen {
    /**
     * Logger for this class.
     */
    private val log = KotlinLogging.logger(KeyGen::class.java.name)
    /**
     * Key generator for blowfish
     */
    private var blowfishKeyGen: KeyGenerator? = null
    /**
     * Public/Static RSA KeyPairs with encrypted modulus N
     */
    private var encryptedRSAKeyPairs: Array<EncryptedRSAKeyPair>? = null

    /**
     * Get common RSA Public/Static Key Pair with encrypted modulus N
     * @return encryptedRSAkeypairs
     */
    val encryptedRSAKeyPair: EncryptedRSAKeyPair
        get() = encryptedRSAKeyPairs!![Random.nextInt(10)]

    /**
     * Initialize Key Generator (Blowfish keygen and RSA keygen)
     * @throws GeneralSecurityException
     */
    @Throws(GeneralSecurityException::class)
    fun init() {

        log.info("Initializing Key Generator...")

        blowfishKeyGen = KeyGenerator.getInstance("Blowfish")

        val rsaKeyPairGenerator = KeyPairGenerator.getInstance("RSA")

        val spec = RSAKeyGenParameterSpec(1024, RSAKeyGenParameterSpec.F4)
        rsaKeyPairGenerator.initialize(spec)
        encryptedRSAKeyPairs = Array(10) {
            EncryptedRSAKeyPair(rsaKeyPairGenerator.generateKeyPair())
        }

        /*for (i in 0..9) {
            encryptedRSAKeyPairs[i] = EncryptedRSAKeyPair(rsaKeyPairGenerator.generateKeyPair())
        }*/

        // Pre-init RSA cipher.. saving about 300ms
        val rsaCipher = Cipher.getInstance("RSA/ECB/nopadding")
        rsaCipher.init(Cipher.DECRYPT_MODE, encryptedRSAKeyPairs!![0].rsaKeyPair.private)
    }

    /**
     * Generate and return blowfish key
     * @return Random generated blowfish key
     */
    fun generateBlowfishKey(): SecretKey {
        return blowfishKeyGen!!.generateKey()
    }
}
