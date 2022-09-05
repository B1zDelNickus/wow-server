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
            //commit()
            Test.insert {
                it[name] = "Andrei"
            }
            //rollback()
            Test.selectAll()
                .map { Test.toModel(it) }
                .single()
                .apply {
                    name shouldBe "Andrei"
                    id shouldNotBe null
                }
        }

    }

    "f:fool around" {

        class Fun(var name: String = "", block: Fun.() -> Unit = {}) {
            init {
                block()
            }
        }

        class MoreFun(var name: String = "") {
            operator fun invoke(newName: String? = null, block: MoreFun.() -> Unit) {
                block()
            }
        }

        val f = Fun {
            name = "Andrei"
        }

        println(f.name)

        val mf = MoreFun()

        mf(newName = "Bill") {
            name = "Andrei again"
        }

        println(mf.name)

        val (a, b) = "1" to "2"

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