package com.avp.wow.game.network.ls.input

import com.avp.wow.game.network.GameNioServer
import com.avp.wow.game.network.client.GameClientConnection
import com.avp.wow.game.network.client.output.OutClientLoginCheckResponse
import com.avp.wow.game.network.factories.GameClientOutputPacketFactory
import com.avp.wow.game.network.factories.GameClientOutputPacketFactory.packetHandler
import com.avp.wow.game.network.ls.GameLsConnection.Companion.State
import com.avp.wow.game.network.ls.GameLsInputPacket
import com.avp.wow.game.network.ls.output.OutRegisterGs
import com.avp.wow.model.auth.Account
import com.avp.wow.service.account.AccountConfig.accountService
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
class InAccountCheckResponse(
    vararg states: State
) : GameLsInputPacket(opCode = OP_CODE, states = states.toList()) {

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
                        packetHandler.handle(OutClientLoginCheckResponse.OP_CODE, true, accountName)
                            ?.let { pck -> gcc.close(pck, true) }

                    } else {

                        log.info { "Account not authed: $accountId" }
                        packetHandler.handle(OutClientLoginCheckResponse.OP_CODE, false, accountName)
                            ?.let { pck -> gcc.close(pck, true) }

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