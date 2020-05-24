package com.avp.wow.network.client.factories

import com.avp.wow.network.BaseOutputPacketHandler
import com.avp.wow.network.client.login.LoginServerOutputPacket
import com.avp.wow.network.client.login.output.OutAuthClient
import com.avp.wow.network.client.login.output.OutEnterGameServer
import com.avp.wow.network.client.login.output.OutGameServersList
import com.avp.wow.network.client.login.output.OutLogin
import io.ktor.util.KtorExperimentalAPI
import kotlin.reflect.KClass

@KtorExperimentalAPI
object LoginServerOutputPacketFactory {

    val packetHandler = BaseOutputPacketHandler<LoginServerOutputPacket>()

    init {
        addPacket(packetClass = OutAuthClient::class, opcode = OutAuthClient.OP_CODE)
        addPacket(packetClass = OutLogin::class, opcode = OutLogin.OP_CODE)
        addPacket(packetClass = OutGameServersList::class, opcode = OutGameServersList.OP_CODE)
        addPacket(packetClass = OutEnterGameServer::class, opcode = OutEnterGameServer.OP_CODE)
    }

    private fun addPacket(packetClass: KClass<out LoginServerOutputPacket>, opcode: Int) {
        packetHandler.addPacketPrototype(packetClass, opcode)
    }

}