package com.avp.wow.network.client.factories

import com.avp.wow.network.BaseOutputPacketHandler
import com.avp.wow.network.client.game.GameServerOutputPacket
import com.avp.wow.network.client.game.output.OutAuthClient
import com.avp.wow.network.client.game.output.OutClientLoginCheck
import com.avp.wow.network.client.game.output.OutEnterWorld
import com.avp.wow.network.client.game.output.activity.*
import io.ktor.util.KtorExperimentalAPI
import kotlin.reflect.KClass

@KtorExperimentalAPI
object GameServerOutputPacketFactory {

    val packetHandler = BaseOutputPacketHandler<GameServerOutputPacket>()

    init {
        /**
         * Test activity packets TODO move to tests
         */
        addPacket(packetClass = OutActivity1::class, opcode = OutActivity1.OP_CODE)
        addPacket(packetClass = OutActivity2::class, opcode = OutActivity2.OP_CODE)
        addPacket(packetClass = OutActivity3::class, opcode = OutActivity3.OP_CODE)
        addPacket(packetClass = OutActivity4::class, opcode = OutActivity4.OP_CODE)
        addPacket(packetClass = OutActivity5::class, opcode = OutActivity5.OP_CODE)

        addPacket(packetClass = OutAuthClient::class, opcode = OutAuthClient.OP_CODE)
        addPacket(packetClass = OutClientLoginCheck::class, opcode = OutClientLoginCheck.OP_CODE)
        addPacket(packetClass = OutEnterWorld::class, opcode = OutEnterWorld.OP_CODE)

    }

    private fun addPacket(packetClass: KClass<out GameServerOutputPacket>, opcode: Int) {
        packetHandler.addPacketPrototype(packetClass, opcode)
    }

}