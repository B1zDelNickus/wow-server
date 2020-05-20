package com.avp.wow.login.network.gs.input

import com.avp.wow.login.network.client.LoginClientConnection
import com.avp.wow.login.network.client.SessionKey
import com.avp.wow.login.network.gs.LoginGsConnection
import com.avp.wow.login.network.gs.LoginGsInputPacket
import com.avp.wow.login.network.gs.output.OutAccountCheckResponse
import com.avp.wow.service.auth.AuthConfig.authService
import io.ktor.util.KtorExperimentalAPI
import java.nio.ByteBuffer

@KtorExperimentalAPI
class InAccountCheck(
    buffer: ByteBuffer,
    client: LoginGsConnection
) : LoginGsInputPacket(
    opCode = OP_CODE,
    client = client,
    buffer = buffer
) {

    private var sessionId = 0
    private var accountId: Long = 0
    private var loginOk: Int = 0
    private var playOk1: Int = 0
    private var playOk2: Int = 0

    override fun readImpl() {
        sessionId = readD()
        accountId = readQ()
        loginOk = readD()
        playOk1 = readD()
        playOk2 = readD()
    }

    override suspend fun runImpl() {
        connection?.let { con ->
            when (con.sessionId) {
                sessionId -> {

                    authService.checkAccount {

                        val requestedKey = SessionKey(
                            accountId = accountId,
                            loginOk = loginOk,
                            playOk1 = playOk1,
                            playOk2 = playOk2
                        )
                        val lcc = accountsOnLs[accountId] as? LoginClientConnection

                        when {
                            null != lcc && lcc.sessionKey!!.checkSessionKey(requestedKey) -> {

                                /**
                                 * account is successful logged in on gs remove it from here
                                 */
                                accountsOnLs.remove(accountId)

                                val gsi = con.gameServerInfo!!
                                val acc = lcc.account!!

                                /**
                                 * Add account to accounts on GameServer list and update accounts last server
                                 */
                                gsi.accountsOnGs[accountId] = acc

                                // updateLastServer() TODO needed ? probably not

                                sendPacket(
                                    OutAccountCheckResponse(
                                        accountId = accountId,
                                        result = true,
                                        accountName = acc.name,
                                        accessLevel = acc.accessLevel
                                    )
                                )

                            }
                            else -> sendPacket(
                                OutAccountCheckResponse(
                                    accountId = accountId,
                                    result = false,
                                    accountName = null,
                                    accessLevel = 0
                                )
                            )
                        }

                    }

                }
                else -> {

                }
            }
        }

    }

    companion object {
        const val OP_CODE = 0x05
    }
}