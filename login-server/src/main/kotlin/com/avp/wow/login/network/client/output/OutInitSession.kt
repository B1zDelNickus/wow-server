package com.avp.wow.login.network.client.output

import com.avp.wow.login.network.client.LoginClientConnection
import com.avp.wow.login.network.client.LoginClientOutputPacket
import com.avp.wow.network.BaseConnection
import io.ktor.util.KtorExperimentalAPI
import javax.crypto.SecretKey

@KtorExperimentalAPI
class OutInitSession(
    private val sessionId: Int,
    private val publicRsaKey: ByteArray,
    private val blowfishKey: ByteArray
) : LoginClientOutputPacket() {

    constructor(client: LoginClientConnection, blowfishKey: SecretKey) : this(
        sessionId = client.sessionId,
        publicRsaKey = client.encryptedModulus,
        blowfishKey = blowfishKey.encoded
    )

    override fun writeImpl(con: LoginClientConnection) {
        writeD(sessionId) // session id
        writeB(publicRsaKey) // RSA Public Key
        writeB(blowfishKey) // BlowFish key
    }

    override fun <T : BaseConnection> afterWrite(con: T) {
        con.enableEncryption(blowfishKey)
    }

    companion object {
        const val OP_CODE = 1
    }

}