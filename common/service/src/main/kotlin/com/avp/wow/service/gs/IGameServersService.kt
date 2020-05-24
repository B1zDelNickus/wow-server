package com.avp.wow.service.gs

import com.avp.wow.model.auth.Account
import com.avp.wow.model.gs.GameServer
import com.avp.wow.service.gs.enums.GsRegisterResponse

interface IGameServersService {

    val gameServers: MutableMap<Int, GameServer>

    fun registerGameServer(id: Int, host: String, port: Int, name: String) : GsRegisterResponse

    fun isAccountOnAnyGameServer(account: Account) : Boolean
    fun kickAccountFromGameServer(account: Account)

}