package com.avp.wow.service.auth.impl

import com.avp.wow.model.auth.Account
import com.avp.wow.service.auth.IAuthService
import com.avp.wow.service.auth.enums.AuthResponse

class InMemoryAuthService : IAuthService {

    override val accountsOnLs = HashMap<Long, Any>()

    override fun login(login: String, rawPassword: String, currentIp: String): Pair<AuthResponse, Account> {
        return when (login) {
            ADMIN_ACCOUNT.name -> AuthResponse.AUTHED to ADMIN_ACCOUNT
            else -> AuthResponse.NO_SUCH_ACCOUNT to Account.EMPTY
        }
    }

    override fun login(block: IAuthService.() -> AuthResponse) = this.block()

    override fun loadAccount(name: String): Account? {
        return when (name.toLowerCase()) {
            ADMIN_ACCOUNT.name.toLowerCase() -> ADMIN_ACCOUNT
            else -> null
        }
    }

    companion object {

        val ADMIN_ACCOUNT = Account(id = 1, name = "Admin", passwordHash = "21232f297a57a5a743894a0e4a801fc3")

    }

}