package com.avp.wow.network

interface IConnectionConfig<T : IConnectionFactory> {

    val hostName: String
    val port: Int
    val connectionName: String
    val factory: T

}