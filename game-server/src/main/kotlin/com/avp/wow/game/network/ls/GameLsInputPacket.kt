package com.avp.wow.game.network.ls

import com.avp.wow.network.packet.BaseInputPacket
import io.ktor.util.KtorExperimentalAPI
import java.nio.ByteBuffer

@KtorExperimentalAPI
abstract class GameLsInputPacket(
    opCode: Int,
    client: GameLsConnection,
    buffer: ByteBuffer
) : BaseInputPacket<GameLsConnection>(
    opCode = opCode,
    buffer = buffer
) {

    init {
        connection = client
    }

    override suspend fun run() {
        try {
            runImpl()
        } catch (e: Throwable) {
            log.warn(e) { "error handling ls (${connection?.ip}) message $this" }
        }
    }

    /**
     * Send new AionServerPacket to connection that is owner of this packet. This method is equvalent to: getConnection().sendPacket(msg);
     * @param msg
     */
    protected open fun sendPacket(msg: GameLsOutputPacket) {
        connection?.sendPacket(msg)
            ?: throw IllegalStateException("Connection was not sat properly")
    }

}