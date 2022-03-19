package com.avp.wow.game.network.ls

import com.avp.wow.game.network.ls.GameLsConnection.Companion.State
import com.avp.wow.network.packet.BaseInputPacket
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
abstract class GameLsInputPacket(
    opCode: Int,
    private var states: List<State>
) : BaseInputPacket<GameLsConnection>(opCode = opCode), Cloneable {

    override suspend fun run() {
        try {
            if (isValid())
                runImpl()
        } catch (e: Throwable) {
            log.warn(e) { "error handling ls (${connection?.ip}) message $this" }
        }
    }

    /**
     * Send new GameLsOutputPacket to connection that is owner of this packet. This method is equivalent to: getConnection().sendPacket(msg);
     * @param msg
     */
    protected open fun sendPacket(msg: GameLsOutputPacket) {
        connection?.sendPacket(msg)
            ?: throw IllegalStateException("Connection was not sat properly")
    }

    override fun <InputPacket : BaseInputPacket<GameLsConnection>> clonePacket(): InputPacket? {
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