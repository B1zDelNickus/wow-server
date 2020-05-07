package com.avp.wow.service

enum class ServiceMode {

    IN_MEMORY,
    REAL;

    companion object {

        val DEFAULT = IN_MEMORY

        fun from(name: String) = values()
            .firstOrNull { it.name == name } ?: DEFAULT

    }

}