package com.avp.wow.network.client.game

import com.avp.wow.network.BaseInputPacketHandler
import com.avp.wow.network.BaseState
import io.ktor.util.KtorExperimentalAPI
import java.nio.ByteBuffer

@KtorExperimentalAPI
class GameServerInputPacketHandler  : BaseInputPacketHandler<GameServerConnection, GameServerInputPacket>() {

    override fun getPacket(
        state: BaseState,
        id: Int,
        buf: ByteBuffer,
        con: GameServerConnection
    ): GameServerInputPacket? {

        val res = super.getPacket(state, id, buf, con)

        /*if (con.state == State.IN_GAME && con.getActivePlayer().getPlayerAccount()
                .getAccessLevel() === 5 && NetworkConfig.DISPLAY_PACKETS) {
            log.info(
                "0x" + Integer.toHexString(res.getOpcode()).toUpperCase() + " : " + res.getPacketName()
            )
            PacketSendUtility.sendMessage(
                con.getActivePlayer(),
                ColorChat.colorChat(
                    "0x" + Integer.toHexString(res.getOpcode()).toUpperCase() + " : " + res.getPacketName(),
                    "1 0 5 0"
                )
            )
        }*/

        return res
    }

}