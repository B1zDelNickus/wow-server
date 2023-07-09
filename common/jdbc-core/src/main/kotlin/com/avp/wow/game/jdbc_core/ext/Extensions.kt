package com.avp.wow.game.jdbc_core.ext

import com.avp.wow.game.jdbc_core.DbBuilder
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction

fun <T> DbBuilder.tx(block: Transaction.() -> T) = transaction(this.dataBase, block)