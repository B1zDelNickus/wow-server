package com.avp.wow.network.todo_move

import com.avp.wow.network.IConnectionFactory
import com.avp.wow.network.NioServer
import io.ktor.network.sockets.Socket
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
class LoginConnectionFactory : IConnectionFactory {

    override fun create(socket: Socket, nio: NioServer) =
        LoginConnection(socket = socket, nioServer = nio)

}