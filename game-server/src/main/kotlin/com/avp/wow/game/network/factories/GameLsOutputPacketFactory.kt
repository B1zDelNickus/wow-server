package com.avp.wow.game.network.factories

import com.avp.wow.game.network.ls.GameLsOutputPacket
import com.avp.wow.game.network.ls.output.OutAccountCheck
import com.avp.wow.game.network.ls.output.OutAuthGs
import com.avp.wow.game.network.ls.output.OutRegisterGs
import com.avp.wow.network.BaseOutputPacketHandler
import io.ktor.util.KtorExperimentalAPI
import kotlin.reflect.KClass

@KtorExperimentalAPI
object GameLsOutputPacketFactory {

    val packetHandler = BaseOutputPacketHandler<GameLsOutputPacket>()

    init {
        addPacket(packetClass = OutAuthGs::class, opcode = OutAuthGs.OP_CODE)
        addPacket(packetClass = OutRegisterGs::class, opcode = OutRegisterGs.OP_CODE)
        addPacket(packetClass = OutAccountCheck::class, opcode = OutAccountCheck.OP_CODE)
    }

    private fun addPacket(packetClass: KClass<out GameLsOutputPacket>, opcode: Int) {
        packetHandler.addPacketPrototype(packetClass, opcode)
    }

}