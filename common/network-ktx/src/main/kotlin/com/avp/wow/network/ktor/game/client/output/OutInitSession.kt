package com.avp.wow.network.ktor.game.client.output

import com.avp.wow.network.BaseConnection
import com.avp.wow.network.ktor.game.client.GameClientConnection
import com.avp.wow.network.ktor.game.client.GameClientOutputPacket
import io.ktor.util.KtorExperimentalAPI
import javax.crypto.SecretKey

@KtorExperimentalAPI
class OutInitSession(
    private val sessionId: Int,
    private val publicRsaKey: ByteArray,
    private val blowfishKey: ByteArray
) : GameClientOutputPacket() {

    constructor(client: GameClientConnection, blowfishKey: SecretKey) : this(
        sessionId = client.sessionId,
        publicRsaKey = client.encryptedModulus,
        blowfishKey = blowfishKey.encoded
    )

    override fun writeImpl(con: GameClientConnection) {
        writeD(sessionId) // session id
        writeB(publicRsaKey) // RSA Public Key
        writeB(blowfishKey) // BlowFish key
    }

    override fun <T : BaseConnection> afterWrite(con: T) {
        con.enableEncryption(blowfishKey)
    }

    companion object {
        const val OP_CODE = 0x01
    }

}