package com.avp.wow.network.client.login

import com.avp.wow.network.BaseNioService
import com.avp.wow.network.IKtorConnectionFactory
import io.ktor.network.sockets.Socket
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
class LoginServerConnectionFactory : IKtorConnectionFactory {
    override fun create(socket: Socket, nio: BaseNioService): LoginServerConnection {
        return LoginServerConnection(socket = socket, nio = nio)
    }
}