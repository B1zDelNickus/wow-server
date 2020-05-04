package com.avp.wow.network.ktx.login.client.output

import com.avp.wow.network.ktx.login.client.LoginClientConnection
import com.avp.wow.network.ktx.login.client.LoginClientOutputPacket
import javax.crypto.SecretKey

class SpInit(
    private val sessionId: Int,
    private val publicRsaKey: ByteArray,
    private val blowfishKey: ByteArray
) : LoginClientOutputPacket() {

    constructor(client: LoginClientConnection, blowfishKey: SecretKey) : this(
        sessionId = client.sessionId,
        publicRsaKey = client.getEncryptedModulus,
        blowfishKey = blowfishKey.encoded
    )

    override fun writeImpl(con: LoginClientConnection) {
        log.debug { "Write $this - sessionId:'$sessionId', publicRsaKey:'${publicRsaKey.toTypedArray()}', blowfishKey:'${blowfishKey.toTypedArray()}'" }
        writeD(sessionId) // session id
        writeB(publicRsaKey) // RSA Public Key
        writeB(blowfishKey) // BlowFish key
    }

    companion object {
        const val OP_CODE = 0x01
    }

}