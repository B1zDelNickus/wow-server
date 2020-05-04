package com.avp.wow.network

import java.nio.channels.SelectionKey

interface IAcceptor {
    fun accept(key: SelectionKey)
}