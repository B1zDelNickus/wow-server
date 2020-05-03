package com.avp.wow.network.ktor.login.client

import com.avp.wow.network.ktor.login.client.sp.SpInit
import io.ktor.util.KtorExperimentalAPI
import kotlin.reflect.KClass

@KtorExperimentalAPI
object ServerPacketsOpcodes {

    private val idSet = mutableSetOf<Int>()

    private val opCodes = mutableMapOf(
        addPacketOpcode(packetClass = SpInit::class, opcode = SpInit.OP_CODE)
    )

    fun getOpcode(packetClass: KClass<out ClientServerPacket>): Int {
        return opCodes[packetClass]
            ?: throw IllegalArgumentException("There is no opcode for $packetClass defined.")
    }

    private fun addPacketOpcode(packetClass: KClass<out ClientServerPacket>, opcode: Int) =
        when {
            opcode < 0 -> throw IllegalArgumentException(String.format("Packet id must not be below zero but was id 0x%02X", opcode))
            idSet.contains(opcode) -> throw IllegalArgumentException(String.format("There already exists another packet with id 0x%02X", opcode))
            else -> {
                idSet.add(opcode)
                packetClass to opcode
            }
        }

}