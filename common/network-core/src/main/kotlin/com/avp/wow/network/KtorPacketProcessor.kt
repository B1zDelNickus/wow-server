package com.avp.wow.network

import com.avp.wow.network.packet.BaseInputPacket
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import mu.KotlinLogging
import kotlin.coroutines.CoroutineContext

@KtorExperimentalAPI
class KtorPacketProcessor<T : KtorConnection>(
    context: CoroutineContext = Dispatchers.IO
) {

    private val log = KotlinLogging.logger(this::class.java.name)

    private val scope = CoroutineScope(SupervisorJob() + context)

    fun executePacket(packet: BaseInputPacket<T>) {
        scope.launch {
            packet.run()
        }
    }

}