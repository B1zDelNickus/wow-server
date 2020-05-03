package com.avp.wow.network.ktor.login.client

import com.avp.wow.network.IConnectionFactory
import com.avp.wow.network.KtorNioServer
import com.avp.wow.network.ktor.login.client.LoginConnection
import io.ktor.network.sockets.Socket
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
class LoginConnectionFactory : IConnectionFactory {

    override fun create(socket: Socket, nio: KtorNioServer) =
        LoginConnection(socket = socket, nioServer = nio)

}