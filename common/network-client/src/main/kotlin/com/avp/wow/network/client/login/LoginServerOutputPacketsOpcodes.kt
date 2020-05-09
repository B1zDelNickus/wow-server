package com.avp.wow.network.client.login

import com.avp.wow.network.client.login.output.OutAuthClient
import com.avp.wow.network.client.login.output.OutEnterGameServer
import com.avp.wow.network.client.login.output.OutGameServersList
import com.avp.wow.network.client.login.output.OutLogin
import io.ktor.util.KtorExperimentalAPI
import kotlin.reflect.KClass

@KtorExperimentalAPI
object LoginServerOutputPacketsOpcodes {

    private val idSet = mutableSetOf<Int>()

    private val opCodes = mutableMapOf(
        addPacketOpcode(packetClass = OutAuthClient::class, opcode = OutAuthClient.OP_CODE),
        addPacketOpcode(packetClass = OutLogin::class, opcode = OutLogin.OP_CODE),
        addPacketOpcode(packetClass = OutGameServersList::class, opcode = OutGameServersList.OP_CODE),
        addPacketOpcode(packetClass = OutEnterGameServer::class, opcode = OutEnterGameServer.OP_CODE)
    )

    fun getOpcode(packetClass: KClass<out LoginServerOutputPacket>): Int {
        return opCodes[packetClass]
            ?: throw IllegalArgumentException("There is no opcode for $packetClass defined.")
    }

    private fun addPacketOpcode(packetClass: KClass<out LoginServerOutputPacket>, opcode: Int) =
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