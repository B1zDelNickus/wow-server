package com.avp.wow.service

import com.avp.wow.service.ServiceConstants.DEFAULT_SERVICE_MODE
import com.avp.wow.service.ServiceConstants.SERVICE_MODE_KEY

object ServiceConfig {

    val serviceMode = ServiceMode.from(
        name = System.getenv().getOrDefault(SERVICE_MODE_KEY, DEFAULT_SERVICE_MODE)!!
    )

}