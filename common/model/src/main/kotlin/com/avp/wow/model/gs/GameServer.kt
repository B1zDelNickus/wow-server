package com.avp.wow.model.gs

import com.avp.wow.model.auth.Account
import java.util.HashMap

class GameServer {

    val accountsOnGs: MutableMap<Long, Account> = HashMap()

    val isOnline get() = true

    val serverIp get() = byteArrayOf(127, 0, 0, 1)

    val serverPort get() = 3434

}