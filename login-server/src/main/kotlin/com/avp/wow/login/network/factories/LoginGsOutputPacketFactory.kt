package com.avp.wow.login.network.factories

import com.avp.wow.login.network.gs.LoginGsOutputPacket
import com.avp.wow.login.network.gs.output.*
import com.avp.wow.network.BaseOutputPacketHandler
import io.ktor.util.KtorExperimentalAPI
import kotlin.reflect.KClass

@KtorExperimentalAPI
object LoginGsOutputPacketFactory {

    val packetHandler = BaseOutputPacketHandler<LoginGsOutputPacket>()

    init {
        addPacket(packetClass = OutInitSession::class, opcode = OutInitSession.OP_CODE)
        addPacket(packetClass = OutAuthGsOk::class, opcode = OutAuthGsOk.OP_CODE)
        addPacket(packetClass = OutAuthGsFail::class, opcode = OutAuthGsFail.OP_CODE)
        addPacket(packetClass = OutRegisterGsOk::class, opcode = OutRegisterGsOk.OP_CODE)
        addPacket(packetClass = OutRegisterGsFail::class, opcode = OutRegisterGsFail.OP_CODE)
        addPacket(packetClass = OutAccountCheckResponse::class, opcode = OutAccountCheckResponse.OP_CODE)
    }

    private fun addPacket(packetClass: KClass<out LoginGsOutputPacket>, opcode: Int) {
        packetHandler.addPacketPrototype(packetClass, opcode)
    }

}