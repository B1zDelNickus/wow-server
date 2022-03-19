package com.avp.wow.network

import com.avp.wow.network.packet.BaseOutputPacket
import io.ktor.util.KtorExperimentalAPI
import mu.KotlinLogging
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

@KtorExperimentalAPI
class BaseOutputPacketHandler<OutputPacket: BaseOutputPacket> {

    private val log = KotlinLogging.logger(this::class.java.name)

    private val idSet = mutableSetOf<Int>()
    private val packetsPrototypes = mutableMapOf<Int, KClass<out OutputPacket>>()

    fun clearPrototypes() {
        idSet.clear()
        packetsPrototypes.clear()
    }

    fun handle(opcode: Int, vararg args: Any?) =
        when (val packet = packetsPrototypes[opcode]?.primaryConstructor?.call(*args)) {
            null -> { unknownPacket(opcode); null }
            else -> packet
        }

    fun addPacketPrototype(packetClass: KClass<out OutputPacket>, opcode: Int) {
        when {
            opcode < 0 -> throw IllegalArgumentException(
                String.format(
                    "Packet id must not be below zero but was id 0x%02X",
                    opcode
                )
            )
            idSet.contains(opcode) -> throw IllegalArgumentException(
                String.format(
                    "There already exists another packet with id 0x%02X",
                    opcode
                )
            )
            else -> {
                idSet.add(opcode)
                packetsPrototypes[opcode] = packetClass
            }
        }
    }

    private fun unknownPacket(id: Int) {
        //if (NetworkConfig.DISPLAY_UNKNOWNPACKETS) { TODO add this option
        log.warn { String.format("Unknown packet requested: 0x%04X", id) }
        //}
    }

}