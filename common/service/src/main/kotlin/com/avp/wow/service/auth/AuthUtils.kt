package com.avp.wow.service.auth

import org.apache.commons.codec.digest.DigestUtils

object AuthUtils {

    fun encodePassword(rawPassword: String): String {
        return DigestUtils.md5Hex(rawPassword)!!
    }

}