package com.avp.wow.game.network.client

import com.avp.wow.game.network.client.GameClientConnection.Companion.State
import com.avp.wow.network.ncrypt.WowCryptEngine
import com.avp.wow.network.utils.Util
import io.ktor.util.KtorExperimentalAPI
import mu.KotlinLogging
import java.nio.ByteBuffer
import java.util.*

@KtorExperimentalAPI
class GameClientInputPacketHandler {

    private val log = KotlinLogging.logger(this::class.java.name)

    private val packetsPrototypes: MutableMap<Int, GameClientInputPacket> = HashMap()

    /**
     * Reads one packet from given ByteBuffer
     * @param data
     * @param client
     * @return AionClientPacket object from binary data
     */
    fun handle(data: ByteBuffer, client: GameClientConnection): GameClientInputPacket? {
        val state: State = client.state
        val id: Int = WowCryptEngine.decodeOpCodec(data.short.toInt()) and 0xffff
        /* Second opcodec. */
        data.position(data.position() + 3)
        return getPacket(state, id, data, client)
    }

    fun addPacketPrototype(packetPrototype: GameClientInputPacket) {
        packetsPrototypes[packetPrototype.opCode] = packetPrototype
    }

    private fun getPacket(
        state: State,
        id: Int,
        buf: ByteBuffer,
        con: GameClientConnection
    ): GameClientInputPacket? {
        val prototype: GameClientInputPacket? = packetsPrototypes[id]
        if (prototype == null) {
            unknownPacket(state, id, buf)
            return null
        }
        val res = prototype.clonePacket()!!
        res.buffer = buf
        res.connection = con

        /*if (con.state == State.IN_GAME && con.getActivePlayer().getPlayerAccount()
                .getAccessLevel() === 5 && NetworkConfig.DISPLAY_PACKETS) {
            log.info(
                "0x" + Integer.toHexString(res.getOpcode()).toUpperCase() + " : " + res.getPacketName()
            )
            PacketSendUtility.sendMessage(
                con.getActivePlayer(),
                ColorChat.colorChat(
                    "0x" + Integer.toHexString(res.getOpcode()).toUpperCase() + " : " + res.getPacketName(),
                    "1 0 5 0"
                )
            )
        }*/

        return res
    }

    /**
     * Logs unknown packet.
     * @param state
     * @param id
     * @param data
     */
    private fun unknownPacket(state: State, id: Int, data: ByteBuffer) {
        //if (NetworkConfig.DISPLAY_UNKNOWNPACKETS) { TODO add this option
            log.warn {
                String.format(
                    "Unknown packet recived from Aion client: 0x%04X, state=%s %n%s",
                    id,
                    state.toString(),
                    Util.toHex(data)
                )
            }
        //}
    }

}