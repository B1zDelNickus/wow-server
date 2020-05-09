package com.avp.wow.network.ktor.login.client

import com.avp.wow.network.BaseNioService
import com.avp.wow.network.IKtorConnectionFactory
import com.avp.wow.network.KtorConnection
import io.ktor.network.sockets.Socket
import io.ktor.util.KtorExperimentalAPI
import kotlin.coroutines.CoroutineContext

@KtorExperimentalAPI
class LoginClientConnectionFactory : IKtorConnectionFactory {

    override fun create(socket: Socket, nio: BaseNioService, context: CoroutineContext): KtorConnection =
        LoginClientConnection(socket = socket, nio = nio)

}