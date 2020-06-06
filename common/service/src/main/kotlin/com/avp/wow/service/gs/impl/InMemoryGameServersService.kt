package com.avp.wow.service.gs.impl

import com.avp.wow.model.auth.Account
import com.avp.wow.model.gs.GameServer
import com.avp.wow.service.gs.IGameServersService
import com.avp.wow.service.gs.enums.GsRegisterResponse

class InMemoryGameServersService : IGameServersService {

    override val gameServers: MutableMap<Int, GameServer> = mutableMapOf()

    override fun registerGameServer(id: Int, host: String, port: Int, name: String) : GsRegisterResponse {
        synchronized(gameServers) {

            if (gameServers.containsKey(id)) return GsRegisterResponse.ALREADY_REGISTERED

            gameServers[id] = GameServer(
                id = id,
                host = host,
                port = port,
                name = name
            )
        }
        return GsRegisterResponse.REGISTERED
    }

    override fun isAccountOnAnyGameServer(account: Account): Boolean {
        gameServers.values.forEach { srv ->
            if (srv.accountsOnGs.containsValue(account))
                return true
        }
        return false
    }

    override fun kickAccountFromGameServer(account: Account) {
        TODO("Not implemented yet")
    }

}