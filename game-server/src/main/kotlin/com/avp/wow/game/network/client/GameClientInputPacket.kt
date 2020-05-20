package com.avp.wow.game.network.client

import com.avp.wow.game.network.client.GameClientConnection.Companion.State
import com.avp.wow.network.packet.BaseInputPacket
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
abstract class GameClientInputPacket(
    opCode: Int,
    private var states: List<State>
) : BaseInputPacket<GameClientConnection>(opCode = opCode), Cloneable {

    override suspend fun run() {
        try {
            if (isValid())
                runImpl()
        } catch (e: Throwable) {
            log.warn(e) { "error handling ls (${connection?.ip}) message $this" }
        }
    }

    fun clonePacket(): GameClientInputPacket? {
        return try {
            super.clone() as GameClientInputPacket
        } catch (e: CloneNotSupportedException) {
            null
        }
    }

    protected fun readS(size: Int): String {
        val string = readS()
        if (string.isEmpty()) {
            readB(size - (string.length * 2 + 2))
        } else {
            readB(size)
        }
        return string
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