package com.avp.wow.game.network.factories

import com.avp.wow.game.network.client.GameClientOutputPacket
import com.avp.wow.game.network.client.output.*
import com.avp.wow.game.network.client.output.activity.*
import com.avp.wow.network.BaseOutputPacketHandler
import io.ktor.util.KtorExperimentalAPI
import kotlin.reflect.KClass

@KtorExperimentalAPI
object GameClientOutputPacketFactory {

    val packetHandler = BaseOutputPacketHandler<GameClientOutputPacket>()

    init {
        /**
         * Test activity packets TODO remove from here and place into tests
         */
        addPacket(packetClass = OutActivity1::class, opcode = OutActivity1.OP_CODE)
        addPacket(packetClass = OutActivity2::class, opcode = OutActivity2.OP_CODE)
        addPacket(packetClass = OutActivity3::class, opcode = OutActivity3.OP_CODE)
        addPacket(packetClass = OutActivity4::class, opcode = OutActivity4.OP_CODE)
        addPacket(packetClass = OutActivity5::class, opcode = OutActivity5.OP_CODE)

        addPacket(packetClass = OutInitSession::class, opcode = OutInitSession.OP_CODE)
        addPacket(packetClass = OutAuthClientOk::class, opcode = OutAuthClientOk.OP_CODE)
        addPacket(packetClass = OutAuthClientFail::class, opcode = OutAuthClientFail.OP_CODE)
        addPacket(packetClass = OutClientLoginCheckResponse::class, opcode = OutClientLoginCheckResponse.OP_CODE)
        addPacket(packetClass = OutEnterWorldOk::class, opcode = OutEnterWorldOk.OP_CODE)
        addPacket(packetClass = OutExitWorld::class, opcode = OutExitWorld.OP_CODE)
    }

    private fun addPacket(packetClass: KClass<out GameClientOutputPacket>, opcode: Int) {
        packetHandler.addPacketPrototype(packetClass, opcode)
    }

}