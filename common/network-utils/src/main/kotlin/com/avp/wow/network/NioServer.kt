package com.avp.wow.network

import io.ktor.network.selector.ActorSelectorManager
import io.ktor.network.sockets.aSocket
import kotlinx.coroutines.Dispatchers
import mu.KotlinLogging
import java.net.InetSocketAddress

class NioServer(
    vararg serverConfig: ServerConfig
) {

    val configs = serverConfig.toList()

    fun connect() {

        try {

            configs.forEach { cfg ->

                val isa = when (cfg.hostName) {
                    "*" -> InetSocketAddress(cfg.port)
                    else -> InetSocketAddress(cfg.hostName, cfg.port)
                }

                val server = aSocket(ActorSelectorManager(Dispatchers.IO))
                    .tcp()
                    .bind(isa)

            }



        } catch (e: Exception) {

        }

    }

    companion object {

        private val log = KotlinLogging.logger(this::class.java.name)

    }

}