package com.avp.wow.network.client.game

import com.avp.wow.network.BaseNioService
import com.avp.wow.network.IKtorConnectionFactory
import io.ktor.network.sockets.Socket
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
class GameServerConnectionFactory : IKtorConnectionFactory {
    override fun create(socket: Socket, nio: BaseNioService): GameServerConnection {
        return GameServerConnection(socket = socket, nio = nio)
    }
}