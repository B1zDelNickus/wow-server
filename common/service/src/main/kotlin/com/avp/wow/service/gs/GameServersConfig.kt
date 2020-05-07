package com.avp.wow.service.gs

import com.avp.wow.service.ServiceConfig
import com.avp.wow.service.ServiceMode
import com.avp.wow.service.gs.impl.InMemoryGameServersService

object GameServersConfig {

    /**
     * Configured GameServersService
     */
    val gameServersService: IGameServersService by lazy {
        when (ServiceConfig.serviceMode) {
            ServiceMode.IN_MEMORY -> InMemoryGameServersService()
            ServiceMode.REAL -> throw IllegalArgumentException("Not implemented yet")
        }
    }


}