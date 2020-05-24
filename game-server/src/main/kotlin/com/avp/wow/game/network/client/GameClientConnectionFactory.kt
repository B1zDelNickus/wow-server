package com.avp.wow.game.network.client

import com.avp.wow.network.BaseNioService
import com.avp.wow.network.IKtorConnectionFactory
import com.avp.wow.network.KtorConnection
import io.ktor.network.sockets.Socket
import io.ktor.util.KtorExperimentalAPI
import kotlin.coroutines.CoroutineContext

@KtorExperimentalAPI
class GameClientConnectionFactory : IKtorConnectionFactory {
    override fun create(socket: Socket, nio: BaseNioService, context: CoroutineContext): KtorConnection<*> {
        return GameClientConnection(socket = socket, nio = nio, context = context)
    }
}