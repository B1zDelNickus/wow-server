package com.avp.wow.service.auth.impl

import com.avp.wow.model.auth.Account
import com.avp.wow.service.account.AccountConfig.accountService
import com.avp.wow.service.auth.AccountUtils.encodePassword
import com.avp.wow.service.auth.AuthConfig
import com.avp.wow.service.auth.IAuthService
import com.avp.wow.service.auth.enums.AuthResponse
import com.avp.wow.service.ban.BanConfig.banService
import com.avp.wow.service.gs.GameServersConfig.gameServersService

class InMemoryAuthService : IAuthService {

    override val accountsOnLs = HashMap<Long, Any>()

    override fun login(
        login: String,
        rawPassword: String,
        currentIp: String,
        client: Any,
        onAlreadyLoggedHandler: (Any) -> Unit,
        applyAccToConnHandler: (Account) -> Unit
    ): AuthResponse {

        /*return when (login) {
            ADMIN_ACCOUNT.name -> AuthResponse.AUTHED to ADMIN_ACCOUNT
            else -> AuthResponse.NO_SUCH_ACCOUNT to Account.EMPTY
        }*/

        if (banService.isBanned(currentIp)) {
            return AuthResponse.BAN_IP
        }

        var account = loadAccount(name = login)

        if (null == account) {
            when {
                AuthConfig.accountAutoCreationEnabled -> {
                    account = accountService.createAccount(name = login, password = rawPassword)
                }
                else -> return AuthResponse.INVALID_PASSWORD
            }
        }

        if (account.accessLevel < AuthConfig.accessLevel && AuthConfig.gmMode) {
            return AuthResponse.GM_ONLY
        }

        // check for paswords beeing equals
        if (account.passwordHash != encodePassword(rawPassword)) {
            return AuthResponse.INVALID_PASSWORD
        }

        /*if (account.getActivated() !== 1) {
            return AionAuthResponse.INVALID_PASSWORD
        }

        // If account expired
        if (AccountTimeController.isAccountExpired(account)) {
            return AionAuthResponse.TIME_EXPIRED
        }

        // if account is banned
        if (AccountTimeController.isAccountPenaltyActive(account)) {
            return AionAuthResponse.BAN_IP
        }

        // if account is restricted to some ip or mask
        if (account.getIpForce() != null) {
            if (!NetworkUtils.checkIPMatching(account.getIpForce(), connection.getIP())) {
                return AionAuthResponse.BAN_IP
            }
        }*/

        synchronized(this) {

            if (gameServersService.isAccountOnAnyGameServer(account = account)) {
                gameServersService.kickAccountFromGameServer(account = account)
                return AuthResponse.ALREADY_LOGGED_IN
            }

            if (accountsOnLs.containsKey(account.id!!)) {
                accountsOnLs.remove(account.id!!)?.apply(onAlreadyLoggedHandler)
                return AuthResponse.ALREADY_LOGGED_IN
            }
            account.apply(applyAccToConnHandler)
            accountsOnLs[account.id!!] = client
        }

        /*AccountTimeController.updateOnLogin(account)

        // if everything was OK
        com.aionemu.loginserver.controller.AccountController.getAccountDAO()
            .updateLastIp(account.getId(), connection.getIP())

        // last mac is updated after receiving packet from gameserver
        com.aionemu.loginserver.controller.AccountController.getAccountDAO().updateMembership(account.getId())*/

        return AuthResponse.AUTHED

    }

    override fun login(block: IAuthService.() -> AuthResponse) = this.block()

    override fun loadAccount(name: String): Account? {
        return when (name.toLowerCase()) {
            ADMIN_ACCOUNT.name.toLowerCase() -> ADMIN_ACCOUNT
            else -> null
        }
    }

    // @Synchronized
    override fun checkAccount(block: IAuthService.() -> Unit) =
        synchronized(this) {
            this.block()
        }

    companion object {

        val ADMIN_ACCOUNT = Account(
            id = 1,
            name = "Admin",
            passwordHash = "21232f297a57a5a743894a0e4a801fc3",
            currentServerId = 1,
            accessLevel = 99
        )

    }

}