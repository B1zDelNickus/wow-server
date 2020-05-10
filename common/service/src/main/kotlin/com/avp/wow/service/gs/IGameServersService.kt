package com.avp.wow.service.gs

import com.avp.wow.model.auth.Account
import com.avp.wow.model.gs.GameServer

interface IGameServersService {

    val gameServers: MutableMap<Int, GameServer>

    fun registerGameServer(id: Int, host: String, port: Int, name: String) : Boolean

    fun isAccountOnAnyGameServer(account: Account) : Boolean
    fun kickAccountFromGameServer(account: Account)

}