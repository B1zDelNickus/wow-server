package com.avp.wow.game.jdbc_core

import com.avp.wow.game.jdbc_core.enums.DataSourceType

object DbConstants {
    const val DEFAULT_POSTGRES_HOST = "localhost"
    const val DEFAULT_POSTGRES_PORT = 5432
    const val DEFAULT_POSTGRES_DATABASE = "postgres"
    const val DEFAULT_POSTGRES_USER = "postgres"
    const val DEFAULT_POSTGRES_PASSWORD = "postgres"
    const val DEFAULT_SCHEMA_TABLE = "schema_history"
    const val DEFAULT_SCHEMA = "public"
    const val DEFAULT_MIGRATION_PATH = "db/migration/general"
    const val DEFAULT_MIN_POOL_SIZE = 1
    const val DEFAULT_MAX_POOL_SIZE = 2
    const val DEFAULT_IDLE_TIMEOUT = 600_000
    const val DEFAULT_MIN_IDLE = 1
    const val DEFAULT_MAX_LIFETIME = 1_800_000
    const val DEFAULT_CACHE_PREP_STMT = true
    const val DEFAULT_STATEMENTS_CACHE_SIZE = 250
    const val DEFAULT_STATEMENTS_CACHE_SQL_LIMIT = 2048
    const val DEFAULT_CONNECTION_TIMEOUT = 5 * 60 * 1_000
    const val DEFAULT_DRIVER_CLASS_NAME = "org.postgresql.Driver"
    const val DEFAULT_DATASOURCE_CLASS_NAME = "org.postgresql.ds.PGSimpleDataSource"
    const val DEFAULT_AUTO_COMMIT = false
    val DEFAULT_DATA_SOURCE_TYPE = DataSourceType.DEFAULT
}