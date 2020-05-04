package com.avp.wow.network.ktor.login.client

import com.avp.wow.network.BaseNioService
import com.avp.wow.network.IKtorConnectionFactory
import io.ktor.network.sockets.Socket
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
class LoginClientConnectionFactory : IKtorConnectionFactory {

    override fun create(socket: Socket, nio: BaseNioService) =
        LoginClientConnection(socket = socket, nio = nio)

}