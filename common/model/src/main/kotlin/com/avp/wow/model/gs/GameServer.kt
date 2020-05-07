package com.avp.wow.model.gs

import com.avp.wow.model.auth.Account
import java.util.HashMap

class GameServer(

    val accountsOnGs: MutableMap<Long, Account> = HashMap()

)