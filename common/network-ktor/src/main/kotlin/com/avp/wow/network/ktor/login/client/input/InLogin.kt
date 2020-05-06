package com.avp.wow.network.ktor.login.client.input

import com.avp.wow.network.ktor.login.client.LoginClientConnection
import com.avp.wow.network.ktor.login.client.LoginClientInputPacket
import io.ktor.util.KtorExperimentalAPI
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.security.GeneralSecurityException
import javax.crypto.Cipher

@KtorExperimentalAPI
class InLogin(
    buffer: ByteBuffer,
    client: LoginClientConnection
) : LoginClientInputPacket(
    opCode = OP_CODE,
    client = client,
    buffer = buffer
) {

    private lateinit var data: ByteArray

    override fun readImpl() {
        if (remainingBytes >= 128) {
            data = readB(128)
        }
    }

    override suspend fun runImpl() {
        try {
            data
        } catch (e: Exception) {
            return
        }

        val decrypted = try {
            val rsaCipher = Cipher.getInstance("RSA/ECB/NoPadding")!!
            rsaCipher.init(Cipher.DECRYPT_MODE, connection?.rsaPrivateKey)
            rsaCipher.doFinal(data, 0, 128)!!
        } catch (e: GeneralSecurityException) {
            //sendPacket(SM_LOGIN_FAIL(AionAuthResponse.SYSTEM_ERROR))
            return
        }

        val resultString = decrypted.toString(Charset.defaultCharset()).trim().split(" ")

        val user = resultString[0]
        val password = resultString[1]

        log.debug { "Auth with login: $user and pass: $password" }

    }

    companion object {
        const val OP_CODE = 0x04
    }
}