package com.avp.wow.network.ktx.login.client

import com.avp.wow.network.ktx.KtxConnection
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel

class LoginConnection(
    socket: SocketChannel
) : KtxConnection(socket = socket, readBufferSize = DEFAULT_R_BUFFER_SIZE, writeBufferSize = DEFAULT_W_BUFFER_SIZE) {


    override val ip: String
        get() = TODO("Not yet implemented")

    override fun close(forced: Boolean) {
        TODO("Not yet implemented")
    }

    override fun onlyClose(): Boolean {
        TODO("Not yet implemented")
    }

    override fun processData(data: ByteBuffer): Boolean {
        TODO("Not yet implemented")
    }

    override fun writeData(data: ByteBuffer): Boolean {
        TODO("Not yet implemented")
    }

    override fun initialized() {
        TODO("Not yet implemented")
    }

    override fun getDisconnectionDelay(): Long {
        TODO("Not yet implemented")
    }

    override fun onDisconnect() {
        TODO("Not yet implemented")
    }

    override fun onServerClose() {
        TODO("Not yet implemented")
    }

    companion object {

        const val DEFAULT_R_BUFFER_SIZE = 8192 * 2
        const val DEFAULT_W_BUFFER_SIZE = 8192 * 2

        enum class State {

            /**
             * Default state
             */
            NONE,

            /**
             * Means that client just connects
             */
            CONNECTED,

            /**
             * Means that clients GameGuard is authenticated
             */
            AUTHED_GG,

            /**
             * Means that client is logged in.
             */
            AUTHED_LOGIN;

            companion object {

                val DEFAULT = NONE

            }

        }

    }

}