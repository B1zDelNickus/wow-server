package com.avp.wow.service.account

import com.avp.wow.service.ServiceConfig
import com.avp.wow.service.ServiceMode
import com.avp.wow.service.account.impl.InMemoryAccountService

object AccountConfig {

    /**
     * Configured AccountService
     */
    val accountService: IAccountService by lazy {
        when (ServiceConfig.serviceMode) {
            ServiceMode.IN_MEMORY -> InMemoryAccountService()
            ServiceMode.REAL -> throw IllegalArgumentException("Not implemented yet")
        }
    }

}