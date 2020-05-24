package com.avp.wow.game.network.ls

import com.avp.wow.game.network.ls.GameLsConnection.Companion.State
import com.avp.wow.network.ncrypt.WowCryptEngine
import com.avp.wow.network.utils.Util
import io.ktor.util.KtorExperimentalAPI
import mu.KotlinLogging
import java.nio.ByteBuffer
import java.util.*

@KtorExperimentalAPI
class GameLsInputPacketHandler {

    private val log = KotlinLogging.logger(this::class.java.name)

    private val packetsPrototypes: MutableMap<Int, GameLsInputPacket> = HashMap()

    /**
     * Reads one packet from given ByteBuffer
     * @param data
     * @param client
     * @return AionClientPacket object from binary data
     */
    fun handle(data: ByteBuffer, client: GameLsConnection): GameLsInputPacket? {
        val state: State = client.state
        val id: Int = WowCryptEngine.decodeOpCodec(data.short.toInt()) and 0xffff
        /* Second opcodec. */
        data.position(data.position() + 3)
        return getPacket(state, id, data, client)
    }

    fun addPacketPrototype(packetPrototype: GameLsInputPacket) {
        packetsPrototypes[packetPrototype.opCode] = packetPrototype
    }

    fun clearPrototypes() {
        packetsPrototypes.clear()
    }

    private fun getPacket(
        state: State,
        id: Int,
        buf: ByteBuffer,
        con: GameLsConnection
    ): GameLsInputPacket? {
        val prototype: GameLsInputPacket? = packetsPrototypes[id]
        if (prototype == null) {
            unknownPacket(state, id, buf)
            return null
        }
        val res = prototype.clonePacket()!!
        res.buffer = buf
        res.connection = con

        return res
    }

    /**
     * Logs unknown packet.
     * @param state
     * @param id
     * @param data
     */
    fun unknownPacket(state: State, id: Int, data: ByteBuffer) {
        log.warn {
            String.format(
                "Unknown packet recived from Aion client: 0x%04X, state=%s %n%s",
                id,
                state.toString(),
                Util.toHex(data)
            )
        }
    }

}