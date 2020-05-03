package com.avp.wow.network.ktx

import com.avp.wow.network.utils.KtxRunnable

class DisconnectionTask(
    private val connection: KtxConnection
): KtxRunnable {

    override suspend fun run() {
        connection.onDisconnect()
    }

}