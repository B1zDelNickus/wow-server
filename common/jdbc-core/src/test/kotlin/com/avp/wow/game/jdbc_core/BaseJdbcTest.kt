package com.avp.wow.game.jdbc_core

import io.kotlintest.specs.AbstractStringSpec
import io.kotlintest.specs.StringSpec
import mu.KotlinLogging
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.output.Slf4jLogConsumer

abstract class BaseJdbcTest(block: AbstractStringSpec.() -> Unit = {}) : StringSpec(block) {

    companion object {

        protected const val DB_NAME = "test"
        protected const val DB_USER = "test"
        protected const val DB_PASS = "test"

        @JvmStatic
        protected val log by lazy { KotlinLogging.logger("test-logger") }

        @JvmStatic
        protected val container by lazy {
            PostgreSQLContainer<Nothing>("postgres:latest")
                .apply {
                    withDatabaseName(DB_NAME)
                    withUsername(DB_USER)
                    withPassword(DB_PASS)
                    withLogConsumer(Slf4jLogConsumer(log))
                    start()
                }
        }

    }

}