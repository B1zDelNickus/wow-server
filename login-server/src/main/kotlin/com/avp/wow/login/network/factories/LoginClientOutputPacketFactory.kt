package com.avp.wow.login.network.factories

import com.avp.wow.login.network.client.LoginClientOutputPacket
import com.avp.wow.login.network.client.output.*
import com.avp.wow.network.BaseOutputPacketHandler
import io.ktor.util.KtorExperimentalAPI
import kotlin.reflect.KClass

@KtorExperimentalAPI
object LoginClientOutputPacketFactory {

    val packetHandler = BaseOutputPacketHandler<LoginClientOutputPacket>()

    init {
        addPacket(packetClass = OutInitSession::class, opcode = OutInitSession.OP_CODE)
        addPacket(packetClass = OutAuthClientOk::class, opcode = OutAuthClientOk.OP_CODE)
        addPacket(packetClass = OutLoginOk::class, opcode = OutLoginOk.OP_CODE)
        addPacket(packetClass = OutLoginFail::class, opcode = OutLoginFail.OP_CODE)
        addPacket(packetClass = OutEnterGameServerOk::class, opcode = OutEnterGameServerOk.OP_CODE)
        addPacket(packetClass = OutAuthClientFail::class, opcode = OutAuthClientFail.OP_CODE)
    }

    private fun addPacket(packetClass: KClass<out LoginClientOutputPacket>, opcode: Int) {
        packetHandler.addPacketPrototype(packetClass, opcode)
    }

}