package com.avp.wow.network.ktor.login.client.input

import com.avp.wow.model.auth.Account
import com.avp.wow.network.ktor.login.client.LoginClientConnection
import com.avp.wow.network.ktor.login.client.LoginClientConnection.Companion.State.AUTHED_LOGIN
import com.avp.wow.network.ktor.login.client.LoginClientInputPacket
import com.avp.wow.network.ktor.login.client.SessionKey
import com.avp.wow.network.ktor.login.client.output.OutLoginOk
import com.avp.wow.service.auth.AuthConfig
import com.avp.wow.service.auth.AuthUtils
import com.avp.wow.service.auth.enums.AuthResponse
import com.avp.wow.service.ban.BanConfig
import com.avp.wow.service.gs.GameServersConfig
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

        val resultString = decrypted
            .filter { it.toInt() != 0 }
            .toByteArray()
            .toString(Charset.defaultCharset())
            .trim()
            .split(" ")

        val user = resultString[0]
        val password = resultString[1]

        log.debug { "Auth with login: $user and pass: $password" }

        /*val response =
            AuthConfig.authService.login(login = user, rawPassword = password, currentIp = connection!!.ip)

        when (response.first) {
            AuthResponse.AUTHED -> {
                connection?.let { con ->
                    con.state = AUTHED_LOGIN
                    con.sessionKey = SessionKey(account = response.second)
                        .also { key ->
                            con.sendPacket(OutLoginOk(sessionKey = key))
                        }
                }
                log.debug { "User $user authorized to Login Server." }
            }
            AuthResponse.INVALID_PASSWORD -> {

            }
            else -> {

            }
        }*/

        val response =
            AuthConfig.authService.login {

                if (BanConfig.banService.isBanned(connection!!.ip)) {
                    return@login AuthResponse.BAN_IP
                }

                var account = loadAccount(name = user)

                if (null == account) {
                    when {
                        AuthConfig.accountAutoCreationEnabled -> {
                            account = Account.EMPTY // create
                        }
                        else -> return@login AuthResponse.INVALID_PASSWORD
                    }
                }

                /*if (account.getAccessLevel() < Config.MAINTENANCE_MOD_GMLEVEL && Config.MAINTENANCE_MOD) {
                    return AionAuthResponse.GM_ONLY
                }*/

                // check for paswords beeing equals
                if (account.passwordHash != AuthUtils.encodePassword(password)) {
                    return@login AuthResponse.INVALID_PASSWORD
                }

                /*if (account.getActivated() !== 1) {
                    return AionAuthResponse.INVALID_PASSWORD
                }

                // If account expired

                // If account expired
                if (AccountTimeController.isAccountExpired(account)) {
                    return AionAuthResponse.TIME_EXPIRED
                }

                // if account is banned

                // if account is banned
                if (AccountTimeController.isAccountPenaltyActive(account)) {
                    return AionAuthResponse.BAN_IP
                }

                // if account is restricted to some ip or mask

                // if account is restricted to some ip or mask
                if (account.getIpForce() != null) {
                    if (!NetworkUtils.checkIPMatching(account.getIpForce(), connection.getIP())) {
                        return AionAuthResponse.BAN_IP
                    }
                }*/

                synchronized(this) {

                    if (GameServersConfig.gameServersService.isAccountOnAnyGameServer(account = account)) {
                        GameServersConfig.gameServersService.kickAccountFromGameServer(account = account)
                        return@login AuthResponse.ALREADY_LOGGED_IN
                    }

                    if (accountsOnLs.containsKey(account.id!!)) {
                        val prevConnection = accountsOnLs.remove(account.id!!) as LoginClientConnection
                        //prevConnection.closeNow()
                        return@login AuthResponse.ALREADY_LOGGED_IN
                    }

                    connection?.account = account
                    accountsOnLs[account.id!!] = connection!!
                }

                /*AccountTimeController.updateOnLogin(account)

                // if everything was OK
                com.aionemu.loginserver.controller.AccountController.getAccountDAO()
                    .updateLastIp(account.getId(), connection.getIP())

                // last mac is updated after receiving packet from gameserver
                com.aionemu.loginserver.controller.AccountController.getAccountDAO().updateMembership(account.getId())*/

                return@login AuthResponse.AUTHED
            }

        when (response) {
            AuthResponse.AUTHED -> {
                connection?.let { con ->
                    con.state = AUTHED_LOGIN
                    con.sessionKey = SessionKey(account = con.account!!)
                        .also { key ->
                            con.sendPacket(
                                OutLoginOk(
                                    sessionKey = key
                                )
                            )
                        }
                }
                log.debug { "User $user authorized to Login Server." }
            }
            AuthResponse.INVALID_PASSWORD -> {

            }
            else -> {

            }
        }

    }

    companion object {
        const val OP_CODE = 0x04
    }
}