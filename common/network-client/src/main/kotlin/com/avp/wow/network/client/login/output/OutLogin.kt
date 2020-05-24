package com.avp.wow.network.client.login.output

import com.avp.wow.network.client.login.LoginServerConnection
import com.avp.wow.network.client.login.LoginServerOutputPacket
import io.ktor.util.KtorExperimentalAPI
import java.nio.charset.Charset
import java.security.GeneralSecurityException
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.Cipher

@KtorExperimentalAPI
class OutLogin(
    private val login: String,
    private val password: String,
    server: LoginServerConnection
) : LoginServerOutputPacket() {

    lateinit var decrypted: ByteArray

    init {

        // TODO on client side NICK and PASS length max=32
        val data = "$login $password".toByteArray(Charset.defaultCharset())
            .also { b ->
                b + ByteArray(64 - b.size) { ' '.toByte() }
            }

        try {
            val pubKey = KeyFactory.getInstance("RSA")
                .generatePublic(X509EncodedKeySpec(server.publicRsa))!!
            val rsaCipher = Cipher.getInstance("RSA/ECB/NoPadding")!!
            rsaCipher.init(Cipher.ENCRYPT_MODE, pubKey)
            val cipherBytes = rsaCipher.doFinal(data)!!//, 0, 64)

            val cipherText: String = Base64.getEncoder().encodeToString(cipherBytes)
            decrypted = Base64.getDecoder().decode(cipherText)

        } catch (e: GeneralSecurityException) {
            // TODO handle ERROR
            log.error(e) { "Encrypting credentials error: ${e.message}." }
        }

    }

    override fun writeImpl(con: LoginServerConnection) {
        writeD(con.sessionId)
        writeB(decrypted) // encrypted crdentials id
        //writeS(login)
        //writeS(password)
    }

    companion object {
        const val OP_CODE = 0x04
    }

}