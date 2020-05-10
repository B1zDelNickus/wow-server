package com.avp.wow.network.ktor.login.gs

import com.avp.wow.network.ktor.login.gs.output.OutAccountCheckResponse
import com.avp.wow.network.ktor.login.gs.output.OutAuthGsOk
import com.avp.wow.network.ktor.login.gs.output.OutInitSession
import com.avp.wow.network.ktor.login.gs.output.OutRegisterGsOk
import io.ktor.util.KtorExperimentalAPI
import kotlin.reflect.KClass

@KtorExperimentalAPI
object LoginGsOutputPacketsOpcodes {

    private val idSet = mutableSetOf<Int>()

    private val opCodes = mutableMapOf(
        addPacketOpcode(packetClass = OutInitSession::class, opcode = OutInitSession.OP_CODE),
        addPacketOpcode(packetClass = OutAuthGsOk::class, opcode = OutAuthGsOk.OP_CODE),
        addPacketOpcode(packetClass = OutRegisterGsOk::class, opcode = OutRegisterGsOk.OP_CODE),
        addPacketOpcode(packetClass = OutAccountCheckResponse::class, opcode = OutAccountCheckResponse.OP_CODE)
    )

    fun getOpcode(packetClass: KClass<out LoginGsOutputPacket>): Int {
        return opCodes[packetClass]
            ?: throw IllegalArgumentException("There is no opcode for $packetClass defined.")
    }

    private fun addPacketOpcode(packetClass: KClass<out LoginGsOutputPacket>, opcode: Int) =
        when {
            opcode < 0 -> throw IllegalArgumentException(String.format("Packet id must not be below zero but was id 0x%02X", opcode))
            idSet.contains(opcode) -> throw IllegalArgumentException(String.format("There already exists another packet with id 0x%02X", opcode))
            else -> {
                idSet.add(opcode)
                packetClass to opcode
            }
        }

}