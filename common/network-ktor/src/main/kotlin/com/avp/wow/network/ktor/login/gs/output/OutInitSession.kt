package com.avp.wow.network.ktor.login.gs.output

import com.avp.wow.network.BaseConnection
import com.avp.wow.network.ktor.login.client.LoginClientOutputPacket
import com.avp.wow.network.ktor.login.client.LoginClientConnection
import com.avp.wow.network.ktor.login.gs.LoginGsConnection
import com.avp.wow.network.ktor.login.gs.LoginGsOutputPacket
import io.ktor.util.KtorExperimentalAPI
import javax.crypto.SecretKey

@KtorExperimentalAPI
class OutInitSession(
    private val sessionId: Int,
    private val publicRsaKey: ByteArray,
    private val blowfishKey: ByteArray
) : LoginGsOutputPacket() {

    constructor(client: LoginGsConnection, blowfishKey: SecretKey) : this(
        sessionId = client.sessionId,
        publicRsaKey = client.encryptedModulus,
        blowfishKey = blowfishKey.encoded
    )

    override fun writeImpl(con: LoginGsConnection) {
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