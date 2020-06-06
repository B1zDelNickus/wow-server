package com.avp.wow.login

object LoginServerConstants {

    const val PROCESSOR_MIN_THREADS_KEY = "PROCESSOR_MIN_THREADS"
    const val PROCESSOR_MAX_THREADS_KEY = "PROCESSOR_MAX_THREADS"
    const val THREADS_SPAWN_THRESHOLD_KEY = "THREADS_SPAWN_THRESHOLD"
    const val THREADS_KILL_THRESHOLD_KEY = "THREADS_KILL_THRESHOLD"

    const val DEFAULT_PROCESSOR_MIN_THREADS = 1
    const val DEFAULT_PROCESSOR_MAX_THREADS = 8
    const val DEFAULT_THREADS_SPAWN_THRESHOLD = 500
    const val DEFAULT_THREADS_KILL_THRESHOLD = 3

}