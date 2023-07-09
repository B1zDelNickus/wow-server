package com.avp.wow.game.jdbc_core

import com.avp.wow.game.jdbc_core.enums.DataSourceType
import com.mchange.v2.c3p0.ComboPooledDataSource
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import mu.KotlinLogging
import org.apache.commons.dbcp2.BasicDataSource
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import java.sql.Connection
import java.util.*
import javax.sql.DataSource

/**
 * Класс для подключения к PostgresQL
 * @property options Параметры подключения
 */
class DbBuilder(
    private val options: DbOptions = DbOptions()
) {

    private val log = KotlinLogging.logger(this::class.java.name)

    val dataSource by lazy {
        log.info { "Created data source from options: $options" }
        createDataSource(options.dataSourceType)
    }

    val dataBase by lazy {
        Database.connect(dataSource)
    }

    /**
     * Функция выполняет миграцию. Путь к скрипту миграции - options.migrationPath
     */
    fun migrate() {
        log.info { "Start SQL migration using Flyway." }
        try {
            Flyway.configure()
                .schemas(options.schema)
                .dataSource(dataSource)
                .table(options.schemaTable)
                .validateMigrationNaming(true)
                .validateOnMigrate(true)
                .baselineOnMigrate(true)
                .apply {
                    if (!options.migrationIsOff) {
                        locations(*options.migrationPath.map { it }.toTypedArray())
                    } else {
                        locations(*emptyArray<String>())
                    }
                }
                .load()
                .migrate()
            log.info { "Flyway migration was done successfully." }
        } catch (e: Exception) {
            log.error { "Flyway migration failed: ${e.message}!" }
        }
    }

    fun connectDB() {
        log.info { "Connect to DB" }
        dataBase
    }

    /**
     * Коннект к бд из пулла коннектов
     */
    val connection: Connection
        get() {
            return dataSource.connection
        }

    /**
     * Создание базы данных, используется для создания рандомной базы данных в тестах
     */
    fun createDataBase() {
        val tmpDataSource = BasicDataSource().apply {
            url = options.jdbcUrlWithoutDatabase
            username = options.user
            password = options.password
            driverClassName = options.driverClassName
        }
        val statement = tmpDataSource.connection.prepareStatement("CREATE DATABASE ${options.database};")
        statement.execute()
        tmpDataSource.close()
    }

    /**
     * Удаление базы данных, используется для удаления тестовой бд при успешной отработке тестов
     */
    fun dropDataBase() {
        when (dataSource) {
            is ComboPooledDataSource -> (dataSource as ComboPooledDataSource).close()
            is HikariDataSource -> (dataSource as HikariDataSource).close()
            is BasicDataSource -> (dataSource as BasicDataSource).close()
        }
        val tmpDataSource = BasicDataSource().apply {
            url = options.jdbcUrlWithoutDatabase
            username = options.user
            password = options.password
            driverClassName = options.driverClassName
        }
        val statement = tmpDataSource.connection.prepareStatement("DROP DATABASE ${options.database};")
        statement.execute()
        tmpDataSource.close()
    }

    private fun createDataSource(type: DataSourceType): DataSource {
        return when (type) {
            DataSourceType.DBCP -> bBCPDataSource
            DataSourceType.C3P0 -> c3p0DataSource
            DataSourceType.HIKARI -> hikariDataSource
        }
    }

    fun shutdown() {
        when (dataSource) {
            is ComboPooledDataSource -> (dataSource as ComboPooledDataSource).close()
            is HikariDataSource -> (dataSource as HikariDataSource).close()
            is BasicDataSource -> (dataSource as BasicDataSource).close()
        }
    }

    private val bBCPDataSource by lazy {
        BasicDataSource().apply {
            url = options.jdbcUrl
            username = options.user
            password = options.password
            driverClassName = options.driverClassName

            minIdle = options.minPoolSize
            maxIdle = options.maxPoolSize
            maxOpenPreparedStatements = options.prepStmtCacheSize
        }
    }

    private val c3p0DataSource by lazy {
        ComboPooledDataSource().apply {
            jdbcUrl = options.jdbcUrl
            user = options.user
            password = options.password
            driverClass = options.driverClassName

            minPoolSize = options.minPoolSize
            maxPoolSize = options.maxPoolSize
            maxStatements = options.prepStmtCacheSize

        }
    }

    private val hikariDataSource by lazy {
        val hikariProps = Properties().apply {
            this["dataSourceClassName"] = options.dataSourceClassName
            this["dataSource.url"] = options.jdbcUrl
            this["dataSource.user"] = options.user
            this["dataSource.password"] = options.password
            this["autoCommit"] = options.autoCommit
            this["connectionTimeout"] = options.connectionTimeout
            this["idleTimeout"] = options.idleTimeout
            this["maxLifetime"] = options.maxLifetime
            this["minimumIdle"] = options.minIdle
            this["maximumPoolSize"] = options.maxPoolSize
        }
        HikariDataSource(
            HikariConfig(hikariProps)
        )
    }
}