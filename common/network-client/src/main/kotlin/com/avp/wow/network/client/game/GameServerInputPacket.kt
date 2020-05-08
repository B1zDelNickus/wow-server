package com.avp.wow.network.client.game

import com.avp.wow.network.client.game.GameServerConnection.Companion.State
import com.avp.wow.network.packet.BaseInputPacket
import io.ktor.util.KtorExperimentalAPI
import java.nio.ByteBuffer

@KtorExperimentalAPI
abstract class GameServerInputPacket(
    opCode: Int,
    client: GameServerConnection,
    buffer: ByteBuffer,
    vararg states: State
) : BaseInputPacket<GameServerConnection>(
    opCode = opCode,
    buffer = buffer
), Cloneable {

    private val states = states.toList()

    init {
        connection = client
    }

    override suspend fun run() {
        try {
            if (isValid())
                runImpl()
        } catch (e: Throwable) {
            log.warn(e) { "error handling ls (${connection?.ip}) message $this" }
        }
    }

    fun clonePacket(): GameServerInputPacket? {
        return try {
            super.clone() as GameServerInputPacket
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