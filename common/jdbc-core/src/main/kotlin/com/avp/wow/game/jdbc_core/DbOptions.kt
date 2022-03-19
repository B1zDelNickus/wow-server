package com.avp.wow.game.jdbc_core

import com.avp.wow.game.jdbc_core.DbConstants.DEFAULT_AUTO_COMMIT
import com.avp.wow.game.jdbc_core.DbConstants.DEFAULT_CACHE_PREP_STMT
import com.avp.wow.game.jdbc_core.DbConstants.DEFAULT_CONNECTION_TIMEOUT
import com.avp.wow.game.jdbc_core.DbConstants.DEFAULT_DATASOURCE_CLASS_NAME
import com.avp.wow.game.jdbc_core.DbConstants.DEFAULT_DATA_SOURCE_TYPE
import com.avp.wow.game.jdbc_core.DbConstants.DEFAULT_DRIVER_CLASS_NAME
import com.avp.wow.game.jdbc_core.DbConstants.DEFAULT_IDLE_TIMEOUT
import com.avp.wow.game.jdbc_core.DbConstants.DEFAULT_MAX_LIFETIME
import com.avp.wow.game.jdbc_core.DbConstants.DEFAULT_MAX_POOL_SIZE
import com.avp.wow.game.jdbc_core.DbConstants.DEFAULT_MIGRATION_PATH
import com.avp.wow.game.jdbc_core.DbConstants.DEFAULT_MIN_IDLE
import com.avp.wow.game.jdbc_core.DbConstants.DEFAULT_MIN_POOL_SIZE
import com.avp.wow.game.jdbc_core.DbConstants.DEFAULT_POSTGRES_DATABASE
import com.avp.wow.game.jdbc_core.DbConstants.DEFAULT_POSTGRES_HOST
import com.avp.wow.game.jdbc_core.DbConstants.DEFAULT_POSTGRES_PASSWORD
import com.avp.wow.game.jdbc_core.DbConstants.DEFAULT_POSTGRES_PORT
import com.avp.wow.game.jdbc_core.DbConstants.DEFAULT_POSTGRES_USER
import com.avp.wow.game.jdbc_core.DbConstants.DEFAULT_SCHEMA
import com.avp.wow.game.jdbc_core.DbConstants.DEFAULT_SCHEMA_TABLE
import com.avp.wow.game.jdbc_core.DbConstants.DEFAULT_STATEMENTS_CACHE_SIZE
import com.avp.wow.game.jdbc_core.DbConstants.DEFAULT_STATEMENTS_CACHE_SQL_LIMIT
import com.avp.wow.game.jdbc_core.enums.DataSourceType

data class DbOptions(
    var host: String = DEFAULT_POSTGRES_HOST,
    var port: Int = DEFAULT_POSTGRES_PORT,
    var database: String = DEFAULT_POSTGRES_DATABASE,
    var user: String = DEFAULT_POSTGRES_USER,
    var password: String = DEFAULT_POSTGRES_PASSWORD,
    var schemaTable: String = DEFAULT_SCHEMA_TABLE,
    var schema: String = DEFAULT_SCHEMA,
    var migrationPath: List<String> = listOf(DEFAULT_MIGRATION_PATH),
    var minPoolSize: Int = DEFAULT_MIN_POOL_SIZE,
    var maxPoolSize: Int = DEFAULT_MAX_POOL_SIZE,
    var idleTimeout: Int = DEFAULT_IDLE_TIMEOUT,
    var maxLifetime: Int = DEFAULT_MAX_LIFETIME,
    var minIdle: Int = DEFAULT_MIN_IDLE,
    var driverClassName: String = DEFAULT_DRIVER_CLASS_NAME,
    var dataSourceClassName: String = DEFAULT_DATASOURCE_CLASS_NAME,
    var cachePrepStmts: Boolean = DEFAULT_CACHE_PREP_STMT,
    var prepStmtCacheSize: Int = DEFAULT_STATEMENTS_CACHE_SIZE,
    var prepStmtCacheSqlLimit: Int = DEFAULT_STATEMENTS_CACHE_SQL_LIMIT,
    var connectionTimeout: Int = DEFAULT_CONNECTION_TIMEOUT,
    var autoCommit: Boolean = DEFAULT_AUTO_COMMIT,
    var dataSourceType: DataSourceType = DEFAULT_DATA_SOURCE_TYPE,
    var migrationIsOff: Boolean = false
) {
    var fullJdbcUri
        get() = "jdbc:postgresql://$user:$password@$host:$port/$database?currentSchema=$schema"
        set(value) = parseUri(value)

    var jdbcUrl
        get() = "jdbc:postgresql://$host:$port/$database?currentSchema=$schema"
        set(value) = parseUri(value)

    var jdbcUrlWithoutDatabase
        get() = "jdbc:postgresql://$host:$port/"
        set(value) = parseUri(value)

    private fun parseUri(uri: String) {
        val fullUriRegex = """jdbc:postgresql://(.*):(.*)@(.+):(\d+)/(.+)""".toRegex()
        val uriRegex = """jdbc:postgresql://(.+):(\d+)/(.+)""".toRegex()
        val uriWithoutDatabaseRegex = """jdbc:postgresql://(.+):(\d+)[/]?""".toRegex()

        when {
            uri.matches(fullUriRegex) -> {
                fullUriRegex.matchEntire(uri)?.destructured?.let { (pUser, pPassword, pHost, pPort, pDatabase) ->
                    user = pUser
                    password = pPassword
                    host = pHost
                    port = pPort.toInt()
                    database = pDatabase
                }
            }
            uri.matches(uriRegex) -> {
                uriRegex.matchEntire(uri)?.destructured?.let { (pHost, pPort, pDatabase) ->
                    host = pHost
                    port = pPort.toInt()
                    database = pDatabase
                }
            }
            uri.matches(uriWithoutDatabaseRegex) -> {
                uriWithoutDatabaseRegex.matchEntire(uri)?.destructured?.let { (pHost, pPort) ->
                    host = pHost
                    port = pPort.toInt()
                }
            }
        }
    }

    val isDefault get() = this == DEFAULT

    override fun toString(): String = "{}"//Json.stringify(this)

    companion object {

        val DEFAULT = DbOptions()

        fun fromUri(uri: String) = DbOptions()
            .apply { parseUri(uri) }

    }

}