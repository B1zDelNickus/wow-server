package com.avp.wow.login.network.client

import com.avp.wow.model.auth.Account
import kotlin.random.Random

data class SessionKey(
    /**
     * accountId - will be used for authentication on Game Server side.
     */
    val accountId: Long,
    /**
     * login ok key
     */
    val loginOk: Int,
    /**
     * play ok1 key
     */
    val playOk1: Int,
    /**
     * play ok2 key
     */
    val playOk2: Int
) {

    constructor(account: Account) : this(
        accountId = account.id!!,
        loginOk = Random.nextInt(),
        playOk1 = Random.nextInt(),
        playOk2 = Random.nextInt()
    )

    /**
     * Check if given values are ok.
     * @param accountId
     * @param loginOk
     * @return true if accountId and loginOk match this SessionKey
     */
    fun checkLogin(accountId: Long, loginOk: Int): Boolean {
        return this.accountId == accountId && this.loginOk == loginOk
    }

    /**
     * Check if this SessionKey have the same values.
     * @param key
     * @return true if key match this SessionKey.
     */
    fun checkSessionKey(key: SessionKey): Boolean {
        return playOk1 == key.playOk1
                && accountId == key.accountId
                && playOk2 == key.playOk2
                && loginOk == key.loginOk
    }

}