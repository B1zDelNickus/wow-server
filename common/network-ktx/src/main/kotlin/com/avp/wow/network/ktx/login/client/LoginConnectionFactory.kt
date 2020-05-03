package com.avp.wow.network.ktx.login.client

import com.avp.wow.network.ktx.Dispatcher
import com.avp.wow.network.ktx.IKtxConnectionFactory
import java.nio.channels.SocketChannel

class LoginConnectionFactory : IKtxConnectionFactory {
    override fun create(socket: SocketChannel, dispatcher: Dispatcher): LoginConnection {
        return LoginConnection(socket = socket)
    }
}