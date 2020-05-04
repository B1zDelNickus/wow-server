package com.avp.wow.network.ktx

import com.avp.wow.network.Dispatcher
import com.avp.wow.network.KtxConnection
import kotlinx.coroutines.CoroutineScope

class AcceptDispatcher(
    name: String,
    scope: CoroutineScope
) : Dispatcher(name = name, scope = scope) {

    override fun dispatch() {
        if (selector.select() == 0) {
            selector.selectedKeys().forEach { key ->
                if (key.isValid) accept(key)
            }
        }
    }

    override fun closeConnection(con: KtxConnection) {
        TODO("Not supported operation")
    }

}