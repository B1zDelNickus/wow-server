package com.avp.wow.network.ktor.game.ls

import com.avp.wow.network.ktor.game.ls.output.OutAuthGs
import com.avp.wow.network.ktor.game.ls.output.OutRegisterGs
import io.ktor.util.KtorExperimentalAPI
import kotlin.reflect.KClass

@KtorExperimentalAPI
object GameLsOutputPacketsOpcodes {

    private val idSet = mutableSetOf<Int>()

    private val opCodes = mutableMapOf(
        addPacketOpcode(packetClass = OutAuthGs::class, opcode = OutAuthGs.OP_CODE),
        addPacketOpcode(packetClass = OutRegisterGs::class, opcode = OutRegisterGs.OP_CODE)
    )

    fun getOpcode(packetClass: KClass<out GameLsOutputPacket>): Int {
        return opCodes[packetClass]
            ?: throw IllegalArgumentException("There is no opcode for $packetClass defined.")
    }

    private fun addPacketOpcode(packetClass: KClass<out GameLsOutputPacket>, opcode: Int) =
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
                packetClass to opcode
            }
        }

}