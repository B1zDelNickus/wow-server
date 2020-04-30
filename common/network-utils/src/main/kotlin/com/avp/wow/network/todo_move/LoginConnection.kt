package com.avp.wow.network.todo_move

import com.avp.wow.network.NioServer
import com.avp.wow.network.WoWConnection
import io.ktor.network.sockets.Socket
import io.ktor.util.KtorExperimentalAPI
import io.ktor.utils.io.readUTF8Line
import java.nio.ByteBuffer

@KtorExperimentalAPI
class LoginConnection(
    socket: Socket,
    nioServer: NioServer
) : WoWConnection(socket = socket, nioServer = nioServer) {

    private var state = State.DEFAULT

    override suspend fun dispatch() {
        inputChannel.readUTF8Line()?.also { line ->
            when (line.toLowerCase()) {
                "exit" -> {
                    log.info { "Exit msg received" }
                    nioServer.closeConnection(this)
                }
                else -> log.info { "Msg received: $line" }
            }
        }
    }

    override fun processData(data: ByteBuffer): Boolean {
        TODO("Not yet implemented")
    }

    override fun writeData(data: ByteBuffer): Boolean {
        TODO("Not yet implemented")
    }

    override fun initialized() {
        state = State.CONNECTED
        log.info("Connection attemp from: $ip")

        /*encryptedRSAKeyPair = KeyGen.getEncryptedRSAKeyPair()
        val blowfishKey: SecretKey = KeyGen.generateBlowfishKey()
        cryptEngine = CryptEngine()
        cryptEngine.updateKey(blowfishKey.encoded)*/

        /**
         * Send Init packet
         */
        //sendPacket(SM_INIT(this, blowfishKey))
    }

    override fun getDisconnectionDelay(): Long {
        TODO("Not yet implemented")
    }

    override fun onDisconnect() {
        /**
         * Remove account only if not joined GameServer yet.
         */
    }

    override fun onServerClose() {
        close(forced = true)
    }

    companion object {

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