package com.avp.wow.login

import io.ktor.application.Application
import mu.KotlinLogging
import java.time.Duration
import java.time.Instant

private val log = KotlinLogging.logger("com.avp.wow.login.App")

@Suppress("unused")
fun Application.main() {

    val startTime = Instant.now()

    log.info { "Starting Login Server" }



    log.info { "Login Server started in ${Duration.between(startTime, Instant.now()).toMillis() / 1000} seconds." }

}