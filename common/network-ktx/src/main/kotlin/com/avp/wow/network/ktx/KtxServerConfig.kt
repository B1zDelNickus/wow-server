package com.avp.wow.network.ktx

data class KtxServerConfig(
    val hostName: String,
    val port: Int,
    val connectionName: String,
    val factory: IKtxConnectionFactory
)