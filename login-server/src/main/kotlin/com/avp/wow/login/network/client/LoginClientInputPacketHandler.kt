package com.avp.wow.login.network.client

import com.avp.wow.network.BaseInputPacketHandler
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
class LoginClientInputPacketHandler : BaseInputPacketHandler<LoginClientConnection, LoginClientInputPacket>()