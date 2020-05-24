package com.avp.wow.login.network.client

import com.avp.wow.login.network.client.output.*
import io.ktor.util.KtorExperimentalAPI
import kotlin.reflect.KClass

@KtorExperimentalAPI
object LoginClientOutputPacketsOpcodes {

    private val idSet = mutableSetOf<Int>()

    private val opCodes = mutableMapOf<KClass<out LoginClientOutputPacket>, Int>()

    init {
        addPacketOpcode(packetClass = OutInitSession::class, opcode = OutInitSession.OP_CODE)
        addPacketOpcode(packetClass = OutAuthClientOk::class, opcode = OutAuthClientOk.OP_CODE)
        addPacketOpcode(packetClass = OutLoginOk::class, opcode = OutLoginOk.OP_CODE)
        addPacketOpcode(packetClass = OutLoginFail::class, opcode = OutLoginFail.OP_CODE)
        addPacketOpcode(packetClass = OutEnterGameServerOk::class, opcode = OutEnterGameServerOk.OP_CODE)
        addPacketOpcode(packetClass = OutAuthClientFail::class, opcode = OutAuthClientFail.OP_CODE)
    }

    fun clearOpcodes() {
        idSet.clear()
        opCodes.clear()
    }

    fun getOpcode(packetClass: KClass<out LoginClientOutputPacket>): Int {
        return opCodes[packetClass]
            ?: throw IllegalArgumentException("There is no opcode for $packetClass defined.")
    }

    fun addPacketOpcode(packetClass: KClass<out LoginClientOutputPacket>, opcode: Int) {
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