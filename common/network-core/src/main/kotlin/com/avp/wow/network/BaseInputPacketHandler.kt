package com.avp.wow.network

import com.avp.wow.network.ncrypt.WowCryptEngine
import com.avp.wow.network.packet.BaseInputPacket
import com.avp.wow.network.utils.Util
import io.ktor.util.KtorExperimentalAPI
import mu.KotlinLogging
import java.nio.ByteBuffer
import java.util.HashMap

@KtorExperimentalAPI
abstract class BaseInputPacketHandler<Connection: BaseConnection<*>, InputPacket: BaseInputPacket<Connection>> {

    protected val log = KotlinLogging.logger(this::class.java.name)

    private val idSet = mutableSetOf<Int>()
    protected val packetsPrototypes: MutableMap<Int, InputPacket> = HashMap()

    /**
     * Reads one packet from given ByteBuffer
     * @param data
     * @param client
     * @return AionClientPacket object from binary data
     */
    fun handle(data: ByteBuffer, client: Connection): InputPacket? {
        val state = client.state
        val id = WowCryptEngine.decodeOpCodec(data.short.toInt()) and 0xffff
        /* Second opcodec. */
        data.position(data.position() + 3)
        return getPacket(state, id, data, client)
    }

    fun addPacketPrototype(packetPrototype: InputPacket) {
        when {
            packetPrototype.opCode < 0 -> throw IllegalArgumentException(
                String.format(
                    "Packet id must not be below zero but was id 0x%02X",
                    packetPrototype.opCode
                )
            )
            idSet.contains(packetPrototype.opCode) -> throw IllegalArgumentException(
                String.format(
                    "There already exists another packet with id 0x%02X",
                    packetPrototype.opCode
                )
            )
            else -> {
                idSet.add(packetPrototype.opCode)
                packetsPrototypes[packetPrototype.opCode] = packetPrototype
            }
        }
    }

    fun clearPrototypes() {
        idSet.clear()
        packetsPrototypes.clear()
    }

    open protected fun getPacket(
        state: BaseState,
        id: Int,
        buf: ByteBuffer,
        con: Connection
    ): InputPacket? {
        val prototype: InputPacket? = packetsPrototypes[id]
        if (prototype == null) {
            unknownPacket(state, id, buf)
            return null
        }
        val res = prototype.clonePacket<InputPacket>()!!
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
    fun unknownPacket(state: BaseState, id: Int, data: ByteBuffer) {
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