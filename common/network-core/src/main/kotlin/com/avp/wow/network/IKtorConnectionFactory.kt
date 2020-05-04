package com.avp.wow.network

import io.ktor.network.sockets.Socket
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
interface IKtorConnectionFactory : IConnectionFactory {

    fun create(socket: Socket, nio: BaseNioService): KtorConnection

}