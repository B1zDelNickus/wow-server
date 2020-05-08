package com.avp.wow.service.auth

import com.avp.wow.service.ServiceConfig
import com.avp.wow.service.ServiceMode
import com.avp.wow.service.auth.AuthConstants.ACCOUNT_AUTO_CREATION_KEY
import com.avp.wow.service.auth.AuthConstants.DEFAULT_ACCOUNT_AUTO_CREATION
import com.avp.wow.service.auth.impl.InMemoryAuthService

object AuthConfig {

    val accountAutoCreationEnabled =
        System.getenv().getOrDefault(ACCOUNT_AUTO_CREATION_KEY, DEFAULT_ACCOUNT_AUTO_CREATION)!!
            .toBoolean()

    /**
     * Configured AuthService
     */
    val authService: IAuthService by lazy {
        when (ServiceConfig.serviceMode) {
            ServiceMode.IN_MEMORY -> InMemoryAuthService()
            ServiceMode.REAL -> throw IllegalArgumentException("Not implemented yet")
        }
    }

}