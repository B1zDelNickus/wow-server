package com.avp.wow.network.ktor.login.client.output

import com.avp.wow.network.ktor.login.client.LoginClientOutputPacket
import com.avp.wow.network.ktor.login.client.LoginClientConnection
import com.avp.wow.network.ktor.login.client.SessionKey
import com.avp.wow.service.gs.GameServersConfig
import io.ktor.util.KtorExperimentalAPI
import javax.crypto.SecretKey

@KtorExperimentalAPI
class OutGameServersList : LoginClientOutputPacket() {

    override fun writeImpl(con: LoginClientConnection) {

        GameServersConfig.gameServersService.gameServers


    }

    companion object {
        const val OP_CODE = 0x07
    }

}