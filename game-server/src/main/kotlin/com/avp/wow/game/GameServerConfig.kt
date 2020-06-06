package com.avp.wow.game


import com.avp.wow.game.GameServerConstants.DEFAULT_PROCESSOR_MAX_THREADS
import com.avp.wow.game.GameServerConstants.DEFAULT_PROCESSOR_MIN_THREADS
import com.avp.wow.game.GameServerConstants.DEFAULT_THREADS_KILL_THRESHOLD
import com.avp.wow.game.GameServerConstants.DEFAULT_THREADS_SPAWN_THRESHOLD
import com.avp.wow.game.GameServerConstants.PROCESSOR_MAX_THREADS_KEY
import com.avp.wow.game.GameServerConstants.PROCESSOR_MIN_THREADS_KEY
import com.avp.wow.game.GameServerConstants.THREADS_KILL_THRESHOLD_KEY
import com.avp.wow.game.GameServerConstants.THREADS_SPAWN_THRESHOLD_KEY
import com.avp.wow.model.helpers.envIntOrDefault

object GameServerConfig {

    val processorMinThreads = envIntOrDefault(PROCESSOR_MIN_THREADS_KEY, DEFAULT_PROCESSOR_MIN_THREADS)
    val processorMaxThreads = envIntOrDefault(PROCESSOR_MAX_THREADS_KEY, DEFAULT_PROCESSOR_MAX_THREADS)
    val processorThreadSpawnThreshold = envIntOrDefault(THREADS_SPAWN_THRESHOLD_KEY, DEFAULT_THREADS_SPAWN_THRESHOLD)
    val processorThreadKillThreshold = envIntOrDefault(THREADS_KILL_THRESHOLD_KEY, DEFAULT_THREADS_KILL_THRESHOLD)

}