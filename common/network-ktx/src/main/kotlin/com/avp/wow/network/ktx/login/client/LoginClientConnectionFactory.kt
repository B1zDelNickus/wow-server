package com.avp.wow.network.ktx.login.client

import com.avp.wow.network.Dispatcher
import com.avp.wow.network.IKtxConnectionFactory
import java.nio.channels.SocketChannel

class LoginClientConnectionFactory : IKtxConnectionFactory {
    override fun create(socket: SocketChannel, dispatcher: Dispatcher): LoginClientConnection {
        return LoginClientConnection(socket = socket, dispatcher = dispatcher)
    }
}