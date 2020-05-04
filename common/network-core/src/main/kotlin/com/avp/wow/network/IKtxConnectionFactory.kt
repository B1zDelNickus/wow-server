package com.avp.wow.network

import java.nio.channels.SocketChannel

interface IKtxConnectionFactory : IConnectionFactory {
    fun create(socket: SocketChannel, dispatcher: Dispatcher): KtxConnection
}