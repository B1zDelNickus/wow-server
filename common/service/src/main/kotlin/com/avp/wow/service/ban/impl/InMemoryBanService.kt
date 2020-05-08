package com.avp.wow.service.ban.impl

import com.avp.wow.service.ban.IBanService

class InMemoryBanService : IBanService {
    override fun isBanned(ip: String) = false
}