package com.avp.wow.model.auth

data class Account(
    val id: Long? = null,
    val name: String,
    val passwordHash: String,
    val currentServerId: Int
) {


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Account

        if (name != other.name) return false
        if (passwordHash != other.passwordHash) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + passwordHash.hashCode()
        return result
    }

    override fun toString(): String {
        return """{"name":$name}"""
    }

    val isEmpty get() = this == EMPTY

    companion object {

        val EMPTY = Account(null, "", "", -1)

    }

}