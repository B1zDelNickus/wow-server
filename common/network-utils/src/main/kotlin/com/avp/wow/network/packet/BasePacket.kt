package com.avp.wow.network.packet

abstract class BasePacket(
    val type: PacketType
) {

    var opCode: Int = 0

    constructor(type: PacketType, opCode: Int) : this(type = type) {
        this.opCode = opCode
    }

    private val packetName = this::class.java.simpleName!!

    override fun toString() =
        String.format(TYPE_PATTERN, type.type, opCode, packetName)

    companion object {

        const val TYPE_PATTERN = "[%s] 0x%02X %s"

        enum class PacketType(val type: String) {

            /**
             * Server packet
             */
            SERVER("S"),
            /**
             * Client packet
             */
            CLIENT("C");

        }

    }

}