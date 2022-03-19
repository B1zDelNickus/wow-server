package com.avp.wow.login.network.gs

import com.avp.wow.login.network.gs.LoginGsConnection.Companion.State
import com.avp.wow.network.packet.BaseInputPacket
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
abstract class LoginGsInputPacket(
    opCode: Int,
    private var states: List<State>
) : BaseInputPacket<LoginGsConnection>(opCode = opCode), Cloneable {

    override suspend fun run() {
        try {
            if (isValid())
                runImpl()
        } catch (e: Throwable) {
            log.warn(e) { "error handling ls (${connection?.ip}) message $this" }
        }
    }

    /**
     * Send new LoginGsOutputPacket to connection that is owner of this packet. This method is equivalent to: getConnection().sendPacket(msg);
     * @param msg
     */
    protected open fun sendPacket(msg: LoginGsOutputPacket) {
        connection?.sendPacket(msg)
            ?: throw IllegalStateException("Connection was not sat properly")
    }

    override fun <InputPacket : BaseInputPacket<LoginGsConnection>> clonePacket(): InputPacket? {
        return try {
            @Suppress("UNCHECKED_CAST")
            super.clone() as InputPacket
        } catch (e: CloneNotSupportedException) {
            null
        }
    }

    private fun isValid(): Boolean {
        val state: State = connection!!.state
        val valid: Boolean = states.contains(state)
        if (!valid) {
            log.info("$this wont be processed cuz its valid state don't match current connection state: $state")
        }
        return valid
    }

}