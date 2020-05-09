package com.avp.wow.network.ktor.login.gs.output

import com.avp.wow.network.BaseConnection
import com.avp.wow.network.ktor.login.client.LoginClientOutputPacket
import com.avp.wow.network.ktor.login.client.LoginClientConnection
import com.avp.wow.network.ktor.login.gs.LoginGsConnection
import com.avp.wow.network.ktor.login.gs.LoginGsOutputPacket
import io.ktor.util.KtorExperimentalAPI
import javax.crypto.SecretKey

@KtorExperimentalAPI
class OutAuthGsOk : LoginGsOutputPacket() {

    override fun writeImpl(con: LoginGsConnection) {

    }

    companion object {
        const val OP_CODE = 0x03
    }

}