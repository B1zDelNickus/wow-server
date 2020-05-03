package com.avp.wow.network.ktor

import io.ktor.network.sockets.Socket
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
interface IKtorConnectionFactory {

    fun create(socket: Socket, nio: KtorNioServer): KtorConnection

}