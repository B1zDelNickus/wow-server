package com.avp.wow.network.ktx

import com.avp.wow.network.BaseConnection
import java.nio.channels.SocketChannel

abstract class KtxConnection(
    val socket: SocketChannel,
    readBufferSize: Int,
    writeBufferSize: Int
) : BaseConnection(readBufferSize = readBufferSize, writeBufferSize = writeBufferSize)