package com.avp.wow.network.ktor.game.ls

import com.avp.wow.network.BaseNioService
import com.avp.wow.network.IKtxConnectionFactory
import com.avp.wow.network.KtxConnection
import io.ktor.network.sockets.Socket
import io.ktor.util.KtorExperimentalAPI
import kotlin.coroutines.CoroutineContext

@KtorExperimentalAPI
class GameLsConnectionFactory : IKtxConnectionFactory {
    override fun create(socket: Socket, nio: BaseNioService, context: CoroutineContext): KtxConnection {
        return GameLsConnection(socket = socket, nio = nio, context = context)
    }
}