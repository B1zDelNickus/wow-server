package com.avp.wow.network.client.game

import com.avp.wow.network.BaseNioService
import com.avp.wow.network.IKtorConnectionFactory
import com.avp.wow.network.KtorConnection
import io.ktor.network.sockets.Socket
import io.ktor.util.KtorExperimentalAPI
import kotlin.coroutines.CoroutineContext

@KtorExperimentalAPI
class GameServerConnectionFactory : IKtorConnectionFactory {
    override fun create(socket: Socket, nio: BaseNioService, context: CoroutineContext): KtorConnection<*> {
        return GameServerConnection(socket = socket, nio = nio, context = context)
    }
}