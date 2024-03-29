package com.avp.wow.login.network.client.input

import com.avp.wow.login.network.client.LoginClientConnection
import com.avp.wow.login.network.client.LoginClientConnection.Companion.State
import com.avp.wow.login.network.client.LoginClientConnection.Companion.State.AUTHED_LOGIN
import com.avp.wow.login.network.client.LoginClientInputPacket
import com.avp.wow.login.network.client.SessionKey
import com.avp.wow.login.network.client.output.OutLoginFail
import com.avp.wow.login.network.client.output.OutLoginOk
import com.avp.wow.login.network.factories.LoginClientOutputPacketFactory.packetHandler
import com.avp.wow.service.auth.AuthConfig.authService
import com.avp.wow.service.auth.enums.AuthResponse
import java.nio.charset.Charset
import java.security.GeneralSecurityException
import javax.crypto.Cipher

class InLogin(vararg states: State) : LoginClientInputPacket(OP_CODE, states.toList()) {

    private var sessionId: Int = 0
    private var data: ByteArray? = null

    private var user = ""
    private var passwordHash = ""

    override fun readImpl() {
        sessionId = readD()
        if (remainingBytes >= 128) {
            data = readB(128)
        }
        //user = readS()
        //passwordHash = readS()
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

        val resultString = decrypted
            .filter { it.toInt() != 0 }
            .toByteArray()
            .toString(Charset.defaultCharset())
            .trim()
            .split(" ")

        val user = resultString[0]
        val password = resultString[1]

        //val user = user
        //val password = passwordHash

        log.trace { "Auth with login: $user and pass: $password" }

        connection?.let { con ->

            val response = authService.login(
                login = user,
                rawPassword = password,
                currentIp = con.ip,
                client = con,
                onAlreadyLoggedHandler = { any -> (any as LoginClientConnection).closeNow() },
                applyAccToConnHandler = { acc -> con.account = acc }
            )

            when (response) {
                AuthResponse.AUTHED -> {
                    con.state = AUTHED_LOGIN
                    con.sessionKey = SessionKey(account = con.account!!)
                        .also { key ->
                            packetHandler.handle(OutLoginOk.OP_CODE, key)
                                ?.let { pck -> sendPacket(pck) }
                        }
                    log.debug { "User $user authorized to Login Server." }
                }
                AuthResponse.INVALID_PASSWORD -> {
                    // TODO BRUTE PROTECTION
                    log.debug { "Invalid password for account: $user." }
                    con.sendPacket(packet = OutLoginFail(response = response))
                    packetHandler.handle(OutLoginFail.OP_CODE, response)
                        ?.let { pck -> sendPacket(pck) }
                }
                else -> {
                    log.debug { "Failed to auth LS for reason: ${response.name}." }
                    packetHandler.handle(OutLoginFail.OP_CODE, response)
                        ?.let { pck -> con.close(pck, false) }
                }
            }

        }

    }

    companion object {

        const val OP_CODE = 4

    }
}