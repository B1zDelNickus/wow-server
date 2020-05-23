package com.avp.wow.login.network.client.input

import com.avp.wow.login.network.client.LoginClientConnection
import com.avp.wow.login.network.client.LoginClientConnection.Companion.State.AUTHED_LOGIN
import com.avp.wow.login.network.client.LoginClientInputPacket
import com.avp.wow.login.network.client.SessionKey
import com.avp.wow.login.network.client.output.OutLoginFail
import com.avp.wow.login.network.client.output.OutLoginOk
import com.avp.wow.model.auth.Account
import com.avp.wow.service.account.AccountConfig.accountService
import com.avp.wow.service.auth.AccountUtils.encodePassword
import com.avp.wow.service.auth.AuthConfig.accountAutoCreationEnabled
import com.avp.wow.service.auth.AuthConfig.authService
import com.avp.wow.service.auth.enums.AuthResponse
import com.avp.wow.service.ban.BanConfig.banService
import com.avp.wow.service.gs.GameServersConfig.gameServersService
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

    private var data: ByteArray? = null

    private var user = ""
    private var passwordHash = ""

    override fun readImpl() {
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
                            con.sendPacket(
                                OutLoginOk(
                                    sessionKey = key
                                )
                            )
                        }
                    log.debug { "User $user authorized to Login Server." }
                }
                AuthResponse.INVALID_PASSWORD -> {
                    // TODO BRUTE PROTECTION
                    log.debug { "Invalid password for account: $user." }
                    con.sendPacket(packet = OutLoginFail(response = response))
                }
                else -> {
                    log.debug { "Failed to auth LS for reason: ${response.name}." }
                    con.close(closePacket = OutLoginFail(response = response), forced = false)
                }
            }

        }

    }

    companion object {

        const val OP_CODE = 4

    }
}