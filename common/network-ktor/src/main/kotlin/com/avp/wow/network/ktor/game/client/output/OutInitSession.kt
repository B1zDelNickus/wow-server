package com.avp.wow.network.ktor.game.client.output

import com.avp.wow.network.ktor.game.client.GameClientConnection
import com.avp.wow.network.ktor.game.client.GameClientOutputPacket
import com.avp.wow.network.ktor.login.client.LoginClientOutputPacket
import com.avp.wow.network.ktor.login.client.LoginClientConnection
import com.avp.wow.network.ktor.login.client.SessionKey
import com.avp.wow.network.ncrypt.EncryptionKeyPair
import io.ktor.util.KtorExperimentalAPI
import javax.crypto.SecretKey

@KtorExperimentalAPI
class OutInitSession(
    private val encryptionKey: Int
) : GameClientOutputPacket() {

    override fun writeImpl(con: GameClientConnection) {
        //writeQ(sessionKey.accountId)
        //writeD(sessionKey.loginOk)
    }

    companion object {
        const val OP_CODE = 0x01
    }

}