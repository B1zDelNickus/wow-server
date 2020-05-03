package com.avp.wow.network.utils

/**
 * Class presenting Runnable interface for working into Ktx
 */
interface KtxRunnable {
    /**
     * Thread-like run() method for launching in a coroutine context
     */
    suspend fun run()
}