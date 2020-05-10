package com.avp.wow.service.account

import com.avp.wow.model.auth.Account

interface IAccountService {

    fun loadAccount(id: Long) : Account
    fun getAccount(id: Long, name: String, accessLevel: Byte) : Account

}