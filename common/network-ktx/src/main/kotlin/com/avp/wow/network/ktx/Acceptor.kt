package com.avp.wow.network.ktx

import java.nio.channels.SelectionKey
import java.nio.channels.ServerSocketChannel

class Acceptor(
    private val factory: IKtxConnectionFactory,
    private val nioServer: KtxNioServer
) {

    fun accept(key: SelectionKey) {
        val channel = (key.channel() as ServerSocketChannel)

        val socketChannel = channel.accept()
            .apply { configureBlocking(false) }

        val dispatcher = nioServer.getReadWriteDispatcher()

        val connection = factory.create(socket = socketChannel, dispatcher = dispatcher)

        dispatcher.register(channel = socketChannel, ops = SelectionKey.OP_READ, connection = connection)

        connection.initialized()
    }

}