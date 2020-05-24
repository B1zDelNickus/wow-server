package com.avp.wow.login.network.gs

import com.avp.wow.network.BaseInputPacketHandler
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
class LoginGsInputPacketHandler : BaseInputPacketHandler<LoginGsConnection, LoginGsInputPacket>()