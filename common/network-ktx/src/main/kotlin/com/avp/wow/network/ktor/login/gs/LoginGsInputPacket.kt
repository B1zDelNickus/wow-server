package com.avp.wow.network.ktor.login.gs

import com.avp.wow.network.packet.BaseInputPacket
import io.ktor.util.KtorExperimentalAPI
import java.nio.ByteBuffer

@KtorExperimentalAPI
abstract class LoginGsInputPacket(
    opCode: Int,
    client: LoginGsConnection,
    buffer: ByteBuffer
) : BaseInputPacket<LoginGsConnection>(
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
    protected open fun sendPacket(msg: LoginGsOutputPacket) {
        connection?.sendPacket(msg)
            ?: throw IllegalStateException("Connection was not sat properly")
    }

}