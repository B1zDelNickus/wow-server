package com.avp.wow.network

data class KtxConnectionConfig(
    override val hostName: String,
    override val port: Int,
    override val connectionName: String,
    override val factory: IKtxConnectionFactory
) : IConnectionConfig<IKtxConnectionFactory>