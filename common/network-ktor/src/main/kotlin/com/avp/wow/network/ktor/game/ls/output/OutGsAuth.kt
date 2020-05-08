package com.avp.wow.network.ktor.game.ls.output

import com.avp.wow.network.ktor.game.ls.GameLsConnection
import com.avp.wow.network.ktor.game.ls.GameLsOutputPacket
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
class OutGsAuth : GameLsOutputPacket() {

    override fun writeImpl(con: GameLsConnection) {

    }

    companion object {
        const val OP_CODE = 0x01
    }

}