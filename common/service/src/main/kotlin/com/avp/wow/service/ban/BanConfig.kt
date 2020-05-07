package com.avp.wow.service.ban

import com.avp.wow.service.ServiceConfig
import com.avp.wow.service.ServiceMode
import com.avp.wow.service.ban.impl.InMemoryBanService

object BanConfig {

    /**
     * Configured BanService
     */
    val banService: IBanService by lazy {
        when (ServiceConfig.serviceMode) {
            ServiceMode.IN_MEMORY -> InMemoryBanService()
            ServiceMode.REAL -> throw IllegalArgumentException("Not implemented yet")
        }
    }

}