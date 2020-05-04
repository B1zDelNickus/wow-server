package com.avp.wow.network.client.login

import com.avp.wow.network.packet.BaseInputPacket
import io.ktor.util.KtorExperimentalAPI
import java.nio.ByteBuffer

@KtorExperimentalAPI
abstract class LoginServerInputPacket(
    opCode: Int,
    server: LoginServerConnection,
    buffer: ByteBuffer
) : BaseInputPacket<LoginServerConnection>(
    opCode = opCode,
    buffer = buffer
) {

    init {
        connection = server
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
    protected open fun sendPacket(msg: LoginServerOutputPacket) {
        connection?.sendPacket(msg)
            ?: throw IllegalStateException("Connection was not sat properly")
    }

}