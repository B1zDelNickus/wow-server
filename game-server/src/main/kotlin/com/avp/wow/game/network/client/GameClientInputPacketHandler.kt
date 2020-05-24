package com.avp.wow.game.network.client

import com.avp.wow.network.BaseInputPacketHandler
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
class GameClientInputPacketHandler : BaseInputPacketHandler<GameClientConnection, GameClientInputPacket>()