package com.avp.wow.game.jdbc_core

import com.avp.wow.game.jdbc_core.ext.tx
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

class PostgresBuilderTests : BaseJdbcTest({

    "connect to DB test" {

        val builder = DbBuilder(
            options = DbOptions.fromUri(container.jdbcUrl).copy(
                database = container.databaseName,
                user = container.username,
                password = container.password
            )
        )

        builder.connectDB()

        builder.tx {
            SchemaUtils.create(Test)
            Test.insert {
                it[name] = "Andrei"
            }
            Test.selectAll()
                .map { Test.toModel(it) }
                .single()
                .apply {
                    name shouldBe "Andrei"
                    id shouldNotBe null
                }
        }

    }


}) {
    companion object {
        class Test(
            val id: Int? = null,
            val name: String? = null
        ) {
            companion object : IntIdTable() {
                val name = varchar("name", 50)
                fun toModel(rs: ResultRow) = Test(id = rs[id].value, name = rs[name])
            }
        }
    }
}