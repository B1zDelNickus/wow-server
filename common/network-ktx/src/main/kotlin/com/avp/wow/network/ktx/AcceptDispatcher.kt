package com.avp.wow.network.ktx

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

}