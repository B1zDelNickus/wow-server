package com.avp.wow.network.ktx.login.client

import com.avp.wow.network.packet.BaseInputPacket
import java.nio.ByteBuffer

abstract class LoginClientInputPacket(
    opCode: Int,
    client: LoginClientConnection,
    buffer: ByteBuffer
) : BaseInputPacket<LoginClientConnection>(
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
    protected open fun sendPacket(msg: LoginClientOutputPacket) {
        connection?.sendPacket(msg)
            ?: throw IllegalStateException("Connection was not sat properly")
    }

}