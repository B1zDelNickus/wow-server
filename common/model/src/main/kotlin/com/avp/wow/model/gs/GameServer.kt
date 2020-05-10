package com.avp.wow.model.gs

import com.avp.wow.model.auth.Account

data class GameServer(
    val id: Int,
    val host: String,
    val port: Int,
    val name: String
) {
    val accountsOnGs = mutableMapOf<Long, Account>()
    val isOnline = true
}