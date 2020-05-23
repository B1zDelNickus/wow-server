package com.avp.wow.service.auth

import com.avp.wow.model.auth.Account
import com.avp.wow.service.auth.enums.AuthResponse

interface IAuthService {

    val accountsOnLs: MutableMap<Long, Any>

    fun login(
        login: String,
        rawPassword: String,
        currentIp: String,
        client: Any,
        onAlreadyLoggedHandler: (Any) -> Unit,
        applyAccToConnHandler: (Account) -> Unit
    ) : AuthResponse

    fun login(block: IAuthService.() -> AuthResponse) : AuthResponse

    fun loadAccount(name: String) : Account?

    fun checkAccount(block: IAuthService.() -> Unit)

}