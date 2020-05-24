package com.avp.wow.network.client.login

import com.avp.wow.network.BaseInputPacketHandler
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
class LoginServerInputPacketHandler : BaseInputPacketHandler<LoginServerConnection, LoginServerInputPacket>()