package com.avp.wow.network.ktor.game.ls.input

import com.avp.wow.model.auth.Account
import com.avp.wow.network.ktor.game.GameNioServer
import com.avp.wow.network.ktor.game.client.GameClientConnection
import com.avp.wow.network.ktor.game.client.output.OutClientLoginCheckResponse
import com.avp.wow.network.ktor.game.ls.GameLsConnection
import com.avp.wow.network.ktor.game.ls.GameLsInputPacket
import com.avp.wow.service.account.AccountConfig.accountService
import io.ktor.util.KtorExperimentalAPI
import java.nio.ByteBuffer

@KtorExperimentalAPI
class InAccountCheckResponse(
    buffer: ByteBuffer,
    client: GameLsConnection
) : GameLsInputPacket(
    opCode = OP_CODE,
    client = client,
    buffer = buffer
) {

    private var sessionId: Int = 0
    private var accountId: Long = 0
    private var result: Boolean = false
    private var accountName: String = ""
    private var accessLevel: Byte = 0

    override fun readImpl() {
        sessionId = readD()
        accountId = readQ()
        result = readC() == 1
        if (result) {
            accountName = readS()
            accessLevel = readSC()
        }
    }

    override suspend fun runImpl() {
        connection?.let { con ->
            when (con.sessionId) {
                sessionId -> {

                    val gcc = (con.nio as GameNioServer)
                        .loginServerConnection
                        ?.removeAccountCheckRequest(accountId = accountId)
                        ?: return

                    val acc = accountService.getAccount(
                        id = accountId,
                        name = accountName,
                        accessLevel = accessLevel
                    )

                    if (!validateAccount(account = acc)) {
                        log.info { "Illegal account auth detected: $accountId" }
                        return
                    }

                    if (result) {

                        gcc.account = acc
                        gcc.state = GameClientConnection.Companion.State.AUTHED
                        con.nio.loginServerConnection!!.loggedInAccounts[accountId] = gcc

                        log.info { "Account authed: $accountId = $accountName" }

                        gcc.sendPacket(
                            OutClientLoginCheckResponse(
                                result = true,
                                accountName = accountName
                            )
                        )

                    } else {

                        log.info { "Account not authed: $accountId" }

                        gcc.close(
                            closePacket = OutClientLoginCheckResponse(
                                result = true,
                                accountName = accountName
                            ),
                            forced = true
                        )

                    }

                }
                else -> {

                }
            }
        }
    }

    private fun validateAccount(account: Account) = true

    companion object {
        const val OP_CODE = 0x07
    }

}