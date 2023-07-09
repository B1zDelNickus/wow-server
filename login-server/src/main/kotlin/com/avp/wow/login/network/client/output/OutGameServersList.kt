package com.avp.wow.login.network.client.output

import com.avp.wow.login.network.client.LoginClientConnection
import com.avp.wow.login.network.client.LoginClientOutputPacket
import com.avp.wow.service.gs.GameServersConfig

class OutGameServersList : LoginClientOutputPacket() {

    init {
        opCode = OP_CODE
    }

    override fun writeImpl(con: LoginClientConnection) {
        GameServersConfig.gameServersService.gameServers
    }

    companion object {
        const val OP_CODE = 7
    }

}