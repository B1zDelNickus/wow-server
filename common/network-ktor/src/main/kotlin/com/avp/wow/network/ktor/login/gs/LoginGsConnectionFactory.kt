package com.avp.wow.network.ktor.login.gs

import com.avp.wow.network.BaseNioService
import com.avp.wow.network.IKtorConnectionFactory
import io.ktor.network.sockets.Socket
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
class LoginGsConnectionFactory : IKtorConnectionFactory {
    override fun create(socket: Socket, nio: BaseNioService): LoginGsConnection {
        return LoginGsConnection(socket = socket, nio = nio)
    }
}