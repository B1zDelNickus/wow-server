package com.avp.wow.network.ktor.login.client

import com.avp.wow.network.ktor.packet.BaseClientPacket
import io.ktor.util.KtorExperimentalAPI
import java.nio.ByteBuffer

@KtorExperimentalAPI
abstract class LoginClientPacket(
    opCode: Int,
    client: LoginConnection,
    buffer: ByteBuffer
) : BaseClientPacket<LoginConnection>(
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
    protected open fun sendPacket(msg: LoginServerPacket) {
        connection?.sendPacket(msg)
            ?: throw IllegalStateException("Connection was not sat properly")
    }

}