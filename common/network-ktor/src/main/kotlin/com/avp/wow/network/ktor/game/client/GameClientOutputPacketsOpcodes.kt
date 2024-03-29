package com.avp.wow.network.ktor.game.client

import com.avp.wow.network.ktor.game.client.output.*
import com.avp.wow.network.ktor.game.client.output.activity.*
import io.ktor.util.KtorExperimentalAPI
import kotlin.reflect.KClass

@KtorExperimentalAPI
object GameClientOutputPacketsOpcodes {

    private val idSet = mutableSetOf<Int>()

    private val opCodes = mutableMapOf(
        /**
         * Test activity packets
         */
        addPacketOpcode(packetClass = OutActivity1::class, opcode = OutActivity1.OP_CODE),
        addPacketOpcode(packetClass = OutActivity2::class, opcode = OutActivity2.OP_CODE),
        addPacketOpcode(packetClass = OutActivity3::class, opcode = OutActivity3.OP_CODE),
        addPacketOpcode(packetClass = OutActivity4::class, opcode = OutActivity4.OP_CODE),
        addPacketOpcode(packetClass = OutActivity5::class, opcode = OutActivity5.OP_CODE),

        addPacketOpcode(packetClass = OutInitSession::class, opcode = OutInitSession.OP_CODE),
        addPacketOpcode(packetClass = OutAuthClientOk::class, opcode = OutAuthClientOk.OP_CODE),
        addPacketOpcode(packetClass = OutClientLoginCheckResponse::class, opcode = OutClientLoginCheckResponse.OP_CODE),
        addPacketOpcode(packetClass = OutEnterWorldOk::class, opcode = OutEnterWorldOk.OP_CODE)
    )

    fun getOpcode(packetClass: KClass<out GameClientOutputPacket>): Int {
        return opCodes[packetClass]
            ?: throw IllegalArgumentException("There is no opcode for $packetClass defined.")
    }

    private fun addPacketOpcode(packetClass: KClass<out GameClientOutputPacket>, opcode: Int) =
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