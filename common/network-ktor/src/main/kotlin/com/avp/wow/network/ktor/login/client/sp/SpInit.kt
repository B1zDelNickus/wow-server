package com.avp.wow.network.ktor.login.client.sp

import com.avp.wow.network.ktor.login.client.ClientServerPacket
import com.avp.wow.network.ktor.login.client.LoginConnection
import io.ktor.util.KtorExperimentalAPI
import javax.crypto.SecretKey

@KtorExperimentalAPI
class SpInit(
    private val sessionId: Int,
    private val publicRsaKey: ByteArray,
    private val blowfishKey: ByteArray
) : ClientServerPacket() {

    constructor(client: LoginConnection, blowfishKey: SecretKey) : this(
        sessionId = client.sessionId,
        publicRsaKey = client.getEncryptedModulus,
        blowfishKey = blowfishKey.encoded
    )

    override fun writeImpl(con: LoginConnection) {
        writeD(sessionId) // session id
        writeD(0x0000c621) // protocol revision
        writeB(publicRsaKey) // RSA Public Key
        // unk
        writeD(0x00)
        writeD(0x00)
        writeD(0x00)
        writeD(0x00)

        writeB(blowfishKey) // BlowFish key
        writeD(197635) // unk
        writeD(2097152) // unk
    }

    companion object {
        const val OP_CODE = 0x00
    }

}