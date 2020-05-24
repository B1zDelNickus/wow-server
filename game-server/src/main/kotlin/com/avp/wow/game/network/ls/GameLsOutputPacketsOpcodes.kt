package com.avp.wow.game.network.ls

import com.avp.wow.game.network.ls.output.OutAccountCheck
import com.avp.wow.game.network.ls.output.OutAuthGs
import com.avp.wow.game.network.ls.output.OutRegisterGs
import io.ktor.util.KtorExperimentalAPI
import kotlin.reflect.KClass

@KtorExperimentalAPI
object GameLsOutputPacketsOpcodes {

    private val idSet = mutableSetOf<Int>()

    private val opCodes = mutableMapOf<KClass<out GameLsOutputPacket>, Int>()

    init {
        addPacketOpcode(packetClass = OutAuthGs::class, opcode = OutAuthGs.OP_CODE)
        addPacketOpcode(packetClass = OutRegisterGs::class, opcode = OutRegisterGs.OP_CODE)
        addPacketOpcode(packetClass = OutAccountCheck::class, opcode = OutAccountCheck.OP_CODE)
    }

    fun clearOpcodes() {
        idSet.clear()
        opCodes.clear()
    }

    fun getOpcode(packetClass: KClass<out GameLsOutputPacket>): Int {
        return opCodes[packetClass]
            ?: throw IllegalArgumentException("There is no opcode for $packetClass defined.")
    }

    fun addPacketOpcode(packetClass: KClass<out GameLsOutputPacket>, opcode: Int) {
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
                opCodes[packetClass] = opcode
            }
        }
    }

}