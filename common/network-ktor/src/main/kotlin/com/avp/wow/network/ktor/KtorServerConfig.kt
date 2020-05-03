package com.avp.wow.network.ktor

import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
data class KtorServerConfig(
    val hostName: String,
    val port: Int,
    val connectionName: String,
    val factory: IKtorConnectionFactory
)