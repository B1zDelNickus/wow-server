package com.avp.wow.network

import io.ktor.network.sockets.Socket
import io.ktor.util.KtorExperimentalAPI
import kotlin.coroutines.CoroutineContext

interface IKtorConnectionFactory : IConnectionFactory {
    fun create(socket: Socket, nio: BaseNioService, context: CoroutineContext): KtorConnection<*>
}