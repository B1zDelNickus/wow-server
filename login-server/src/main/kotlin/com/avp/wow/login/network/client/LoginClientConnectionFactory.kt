package com.avp.wow.login.network.client

import com.avp.wow.network.BaseNioService
import com.avp.wow.network.IKtorConnectionFactory
import com.avp.wow.network.KtorConnection
import io.ktor.network.sockets.*
import kotlin.coroutines.CoroutineContext

class LoginClientConnectionFactory : IKtorConnectionFactory {

    override fun create(socket: Socket, nio: BaseNioService, context: CoroutineContext): KtorConnection<*> =
        LoginClientConnection(socket = socket, nio = nio, context = context)

}