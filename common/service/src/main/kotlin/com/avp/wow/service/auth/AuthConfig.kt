package com.avp.wow.service.auth

import com.avp.wow.model.helpers.envBoolOrDefault
import com.avp.wow.model.helpers.envByteOrDefault
import com.avp.wow.service.ServiceConfig
import com.avp.wow.service.ServiceMode
import com.avp.wow.service.auth.AuthConstants.ACCOUNT_AUTO_CREATION_KEY
import com.avp.wow.service.auth.AuthConstants.DEFAULT_ACCOUNT_AUTO_CREATION
import com.avp.wow.service.auth.AuthConstants.DEFAULT_GM_ACCESS_LEVEL
import com.avp.wow.service.auth.AuthConstants.DEFAULT_GM_MODE
import com.avp.wow.service.auth.AuthConstants.GM_ACCESS_LEVEL_KEY
import com.avp.wow.service.auth.AuthConstants.GM_MODE_KEY
import com.avp.wow.service.auth.impl.InMemoryAuthService

object AuthConfig {

    val accountAutoCreationEnabled =
        envBoolOrDefault(ACCOUNT_AUTO_CREATION_KEY, DEFAULT_ACCOUNT_AUTO_CREATION)

    val accessLevel =
        envByteOrDefault(GM_ACCESS_LEVEL_KEY, DEFAULT_GM_ACCESS_LEVEL)

    val gmMode =
        envBoolOrDefault(GM_MODE_KEY, DEFAULT_GM_MODE)


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