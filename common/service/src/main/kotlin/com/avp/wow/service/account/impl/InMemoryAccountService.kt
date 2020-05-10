package com.avp.wow.service.account.impl

import com.avp.wow.model.auth.Account
import com.avp.wow.service.account.IAccountService
import com.avp.wow.service.auth.impl.InMemoryAuthService

class InMemoryAccountService: IAccountService {

    override fun loadAccount(id: Long): Account {
        TODO("Not yet implemented")
    }

    override fun getAccount(id: Long, name: String, accessLevel: Byte): Account {
        return when (id to name.toLowerCase()) {
            id to InMemoryAuthService.ADMIN_ACCOUNT.name.toLowerCase() -> InMemoryAuthService.ADMIN_ACCOUNT
            else -> throw IllegalArgumentException("Account $name with ID:$id was not found.")
        }
    }

}