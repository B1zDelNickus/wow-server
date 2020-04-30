package com.avp.wow.network

import io.ktor.network.sockets.Socket
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
interface IConnectionFactory {

    fun create(socket: Socket, nio: NioServer): WoWConnection

}