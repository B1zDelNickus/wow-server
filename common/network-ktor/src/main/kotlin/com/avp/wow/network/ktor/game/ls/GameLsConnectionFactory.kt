package com.avp.wow.network.ktor.game.ls

import com.avp.wow.network.BaseNioService
import com.avp.wow.network.IKtorConnectionFactory
import io.ktor.network.sockets.Socket
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
class GameLsConnectionFactory : IKtorConnectionFactory {
    override fun create(socket: Socket, nio: BaseNioService): GameLsConnection {
        return GameLsConnection(socket = socket, nio = nio)
    }
}