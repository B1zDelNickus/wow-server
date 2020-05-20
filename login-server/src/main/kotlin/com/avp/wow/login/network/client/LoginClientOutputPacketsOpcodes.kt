package com.avp.wow.login.network.client

import com.avp.wow.login.network.client.output.OutAuthClientOk
import com.avp.wow.login.network.client.output.OutEnterGameServerOk
import com.avp.wow.login.network.client.output.OutInitSession
import com.avp.wow.login.network.client.output.OutLoginOk
import io.ktor.util.KtorExperimentalAPI
import kotlin.reflect.KClass

@KtorExperimentalAPI
object LoginClientOutputPacketsOpcodes {

    private val idSet = mutableSetOf<Int>()

    private val opCodes = mutableMapOf(
        addPacketOpcode(packetClass = OutInitSession::class, opcode = OutInitSession.OP_CODE),
        addPacketOpcode(packetClass = OutAuthClientOk::class, opcode = OutAuthClientOk.OP_CODE),
        addPacketOpcode(packetClass = OutLoginOk::class, opcode = OutLoginOk.OP_CODE),
        addPacketOpcode(packetClass = OutEnterGameServerOk::class, opcode = OutEnterGameServerOk.OP_CODE)
    )

    fun getOpcode(packetClass: KClass<out LoginClientOutputPacket>): Int {
        return opCodes[packetClass]
            ?: throw IllegalArgumentException("There is no opcode for $packetClass defined.")
    }

    private fun addPacketOpcode(packetClass: KClass<out LoginClientOutputPacket>, opcode: Int) =
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