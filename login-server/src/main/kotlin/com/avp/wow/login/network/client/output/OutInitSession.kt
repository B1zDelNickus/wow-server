package com.avp.wow.login.network.client.output

import com.avp.wow.login.network.client.LoginClientConnection
import com.avp.wow.login.network.client.LoginClientOutputPacket
import com.avp.wow.network.BaseConnection
import javax.crypto.SecretKey

class OutInitSession(
    client: LoginClientConnection,
    blowfishKey: SecretKey
) : LoginClientOutputPacket() {

    init {
        opCode = OP_CODE
    }

    private val sessionId = client.sessionId
    private val publicRsaKey = client.encryptedModulus
    private val blowfishKey = blowfishKey.encoded

    override fun writeImpl(con: LoginClientConnection) {
        writeD(sessionId) // session id
        writeB(publicRsaKey) // RSA Public Key
        writeB(blowfishKey) // BlowFish key
    }

    override fun <T : BaseConnection<*>> afterWrite(con: T) {
        con.enableEncryption(blowfishKey)
    }

    companion object {
        const val OP_CODE = 1
    }

}