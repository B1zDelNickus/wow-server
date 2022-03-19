package com.avp.wow.game.jdbc_core.enums

enum class DataSourceType {
    HIKARI,
    C3P0,
    DBCP;

    companion object {

        val DEFAULT = HIKARI

        fun valueOf(source: String) = values()
            .firstOrNull { it.name == source } ?: DEFAULT
    }
}