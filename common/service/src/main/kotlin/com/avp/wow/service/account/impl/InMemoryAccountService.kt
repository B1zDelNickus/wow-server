package com.avp.wow.service.account.impl

import com.avp.wow.model.auth.Account
import com.avp.wow.service.account.IAccountService
import com.avp.wow.service.auth.AccountUtils.encodePassword
import com.avp.wow.service.auth.impl.InMemoryAuthService.Companion.ADMIN_ACCOUNT
import mu.KotlinLogging
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

class InMemoryAccountService: IAccountService {

    private val log = KotlinLogging.logger(this::class.java.name)

    private val accountsHash = ConcurrentHashMap<Long, Account>()
        .apply { put(ADMIN_ACCOUNT.id!!, ADMIN_ACCOUNT) }

    private val idGenerator = AtomicLong(1)

    override fun createAccount(name: String, password: String): Account {
        return Account(
            id = idGenerator.incrementAndGet(),
            name = name,
            passwordHash = encodePassword(password),
            accessLevel = 0,
            currentServerId = 1
        ).also {
            accountsHash[it.id!!] = it
            log.debug { "Account auto created: $name" }
        }
    }

    override fun loadAccount(id: Long): Account {
        TODO("Not yet implemented")
    }

    override fun getAccount(id: Long, name: String, accessLevel: Byte): Account {
        return accountsHash[id]!!
    }

}