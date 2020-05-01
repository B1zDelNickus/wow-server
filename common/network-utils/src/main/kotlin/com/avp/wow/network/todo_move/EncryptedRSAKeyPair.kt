package com.avp.wow.network.todo_move

import java.math.BigInteger
import java.security.KeyPair
import java.security.interfaces.RSAPublicKey
import kotlin.experimental.xor

/**
 * This class is for storing standard RSA Public/Static keyPairs The main difference that N (Modulus) is encrypted to be transfered on the net with simple scrambling algorythm. So public pair (e, n) , where e is exponent (usually static 3 or 65537) and n is modulus, is encrypted and cannot be
 * applied to cipher some data without deciphering the modulus.
 * @author EvilSpirit
 */
class EncryptedRSAKeyPair
/**
 * Default constructor. Stores RSA key pair and encrypts rsa modulus N
 * @param RSAKeyPair standard RSA KeyPair generated with standard KeyPairGenerator [java.security.KeyPairGenerator]
 */
    (
    /**
     * KeyPair
     */
    /**
     * Get default RSA key pair
     * @return RSAKeyPair
     */
    val rsaKeyPair: KeyPair
) {
    /**
     * Byte
     */
    /**
     * Get encrypted modulus to be transferred on the net.
     * @return encryptedModulus
     */
    val encryptedModulus: ByteArray

    init {
        encryptedModulus = encryptModulus((this.rsaKeyPair.public as RSAPublicKey).modulus)
    }

    /**
     * Encrypt RSA modulus N
     * @param modulus RSA modulus from public/private pairs (e,n), (d,n)
     * @return encrypted modulus
     */
    private fun encryptModulus(modulus: BigInteger): ByteArray {
        var encryptedModulus = modulus.toByteArray()

        if (encryptedModulus.size == 0x81 && encryptedModulus[0].toInt() == 0x00) {
            val temp = ByteArray(0x80)

            System.arraycopy(encryptedModulus, 1, temp, 0, 0x80)

            encryptedModulus = temp
        }

        for (i in 0..3) {
            val temp = encryptedModulus[i]

            encryptedModulus[i] = encryptedModulus[0x4d + i]
            encryptedModulus[0x4d + i] = temp
        }

        for (i in 0..63) {
            encryptedModulus[i] = (encryptedModulus[i] xor encryptedModulus[0x40 + i]).toByte()
        }

        for (i in 0..3) {
            encryptedModulus[0x0d + i] = (encryptedModulus[0x0d + i] xor encryptedModulus[0x34 + i]).toByte()
        }

        for (i in 0..63) {
            encryptedModulus[0x40 + i] = (encryptedModulus[0x40 + i] xor encryptedModulus[i]).toByte()
        }

        return encryptedModulus
    }
}
