package com.avp.wow.service.gs

import com.avp.wow.model.auth.Account
import com.avp.wow.model.gs.GameServer

interface IGameServersService {
    val gameServers: Map<Long, GameServer>

    fun isAccountOnAnyGameServer(account: Account) : Boolean
    fun kickAccountFromGameServer(account: Account)

}