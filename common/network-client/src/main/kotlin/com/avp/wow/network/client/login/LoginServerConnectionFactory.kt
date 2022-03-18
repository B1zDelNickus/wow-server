package com.avp.wow.network.client.login

import com.avp.wow.network.BaseNioService
import com.avp.wow.network.IKtxConnectionFactory
import com.avp.wow.network.KtxConnection
import io.ktor.network.sockets.Socket
import io.ktor.util.KtorExperimentalAPI
import kotlin.coroutines.CoroutineContext

@KtorExperimentalAPI
class LoginServerConnectionFactory : IKtxConnectionFactory {
    override fun create(socket: Socket, nio: BaseNioService, context: CoroutineContext): KtxConnection {
        return LoginServerConnection(socket = socket, nio = nio, context = context)
    }
}