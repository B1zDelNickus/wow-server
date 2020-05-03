package com.avp.wow.network.ktx

import io.ktor.network.selector.ActorSelectorManager
import kotlinx.coroutines.CoroutineScope
import java.nio.channels.SelectionKey

class AcceptReadWriteDispatcher(
    name: String,
    scope: CoroutineScope
) : Dispatcher(
    name = name,
    scope = scope
) {

    override fun dispatch() {

        val selected = selector.select()

        // processPendingClose()

        if (selected != 0) {

            selector.selectedKeys().forEach { key ->

                if (key.isValid) {

                    when (key.readyOps()) {

                        SelectionKey.OP_ACCEPT -> {
                            accept(key)
                        }
                        SelectionKey.OP_READ -> {

                        }
                        SelectionKey.OP_WRITE -> {

                        }
                        SelectionKey.OP_READ or SelectionKey.OP_WRITE -> {

                        }

                    }

                }

            }

        }


    }

}