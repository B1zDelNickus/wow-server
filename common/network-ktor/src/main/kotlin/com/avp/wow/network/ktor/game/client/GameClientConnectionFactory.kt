package com.avp.wow.network.ktor.game.client

import com.avp.wow.network.BaseNioService
import com.avp.wow.network.IKtorConnectionFactory
import io.ktor.network.sockets.Socket
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
class GameClientConnectionFactory : IKtorConnectionFactory {
    override fun create(socket: Socket, nio: BaseNioService): GameClientConnection {
        return GameClientConnection(socket = socket, nio = nio)
    }
}