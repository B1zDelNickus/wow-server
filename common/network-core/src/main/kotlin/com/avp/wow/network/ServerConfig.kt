package com.avp.wow.network

data class ServerConfig(
    val hostName: String,
    val port: Int,
    val connectionName: String,
    val factory: IConnectionFactory
)