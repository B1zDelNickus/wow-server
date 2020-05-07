package com.avp.wow.service.auth

import com.avp.wow.model.auth.Account
import com.avp.wow.service.auth.enums.AuthResponse

interface IAuthService {

    val accountsOnLs: MutableMap<Long, Any>

    fun login(login: String, rawPassword: String, currentIp: String) : Pair<AuthResponse, Account>

    fun login(block: IAuthService.() -> AuthResponse) : AuthResponse

    fun loadAccount(name: String) : Account?

}