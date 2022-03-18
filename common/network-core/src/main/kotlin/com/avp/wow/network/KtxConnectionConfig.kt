package com.avp.wow.network

import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
data class KtxConnectionConfig(
    override val hostName: String,
    override val port: Int,
    override val connectionName: String,
    override val factory: IKtxConnectionFactory
) : IConnectionConfig<IKtxConnectionFactory> {

    override fun toString(): String {
        return "[$hostName:$port|$connectionName]"
    }

}