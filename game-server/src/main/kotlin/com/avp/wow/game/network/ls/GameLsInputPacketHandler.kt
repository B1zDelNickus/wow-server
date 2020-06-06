package com.avp.wow.game.network.ls

import com.avp.wow.network.BaseInputPacketHandler
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
class GameLsInputPacketHandler : BaseInputPacketHandler<GameLsConnection, GameLsInputPacket>()