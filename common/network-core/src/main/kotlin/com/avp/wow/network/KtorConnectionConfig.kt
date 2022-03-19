package com.avp.wow.network

data class KtorConnectionConfig(
    override val hostName: String,
    override val port: Int,
    override val connectionName: String,
    override val factory: IKtorConnectionFactory
) : IConnectionConfig<IKtorConnectionFactory> {

    override fun toString(): String {
        return "[$hostName:$port|$connectionName]"
    }

}