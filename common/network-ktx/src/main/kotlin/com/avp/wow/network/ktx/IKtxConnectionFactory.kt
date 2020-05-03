package com.avp.wow.network.ktx

import com.avp.wow.network.IConnectionFactory
import java.nio.channels.SocketChannel

interface IKtxConnectionFactory : IConnectionFactory {
    fun create(socket: SocketChannel, dispatcher: Dispatcher): KtxConnection
}