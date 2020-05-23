package com.avp.wow.network

import com.avp.wow.network.packet.BaseInputPacket
import com.avp.wow.network.utils.KtxRunnable
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.*
import mu.KotlinLogging
import java.util.*
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.coroutines.CoroutineContext

@KtorExperimentalAPI
class KtorPacketProcessor<T: KtorConnection>(
    private val minThreads: Int,
    private val maxThreads: Int,
    private val threadSpawnThreshold: Int,
    private val threadKillThreshold: Int,
    context: CoroutineContext = Dispatchers.IO
) {

    private val log = KotlinLogging.logger(this::class.java.name)

    private val scope by lazy { CoroutineScope(SupervisorJob() + context) }

    /**
     * Lock for synchronization.
     */
    private val lock: Lock = ReentrantLock()

    /**
     * Not Empty condition.
     */
    private val notEmpty = lock.newCondition()

    /**
     * Queue of packet that will be executed in correct order.
     */
    val packets = LinkedList<BaseInputPacket<T>>()

    /**
     * Working threads.
     */
    private val jobs: MutableList<Pair<Job, PacketProcessorTask>> = ArrayList()

    init {

        if (minThreads != maxThreads) {
            startCheckerThread()
        }

        for (i in 0 until minThreads) {
            newJob()
        }

    }

    /**
     * Start Checker Thread. Checker is responsible for increasing / reducing PacketProcessor Thread count based on Runtime needs.
     */
    private fun startCheckerThread() {
        scope.launch { CheckerTask().run() }
    }

    /**
     * Create and start new PacketProcessor Thread, but only if there wont be more working Threads than "maxThreads"
     * @return true if new Thread was created.
     */
    private fun newJob(): Boolean {
        if (jobs.size >= maxThreads) {
            return false
        }
        val name = "PacketProcessor:" + jobs.size
        log.debug("Creating new PacketProcessor Thread: $name")
        val task = PacketProcessorTask(name = name)
        val job = scope.launch(start = CoroutineStart.LAZY) { task.run() }
        task.ownJob = job
        jobs.add(job to task)
        job.start()
        return true
    }

    /**
     * Kill one PacketProcessor Thread, but only if there are more working Threads than "minThreads"
     */
    private fun killThread() {
        if (jobs.size < minThreads) {
            val t: Pair<Job, PacketProcessorTask> = jobs.removeAt(jobs.size - 1)
            log.debug("Killing PacketProcessor Job: " + t.second.name)
            t.first.cancel("kill: ${t.second.name}")
        }
    }

    /**
     * Add packet to execution queue and execute it as soon as possible on another Thread.
     * @param packet that will be executed.
     */
    fun executePacket(packet: BaseInputPacket<T>) {
        lock.lock()
        try {
            packets.add(packet)
            notEmpty.signal()
        } finally {
            lock.unlock()
        }
    }

    /**
     * Return first packet available for execution with respecting rules: - 1 packet / client at one time. - execute packets in received order.
     * @return first available BaseClientPacket
     */
    private fun getFirstAvailable(): BaseInputPacket<T>? {
        while (true) {
            while (packets.isEmpty()) {
                notEmpty.awaitUninterruptibly()
            }
            val it = packets.listIterator()
            while (it.hasNext()) {
                val packet = it.next()
                if (packet.connection!!.tryLockConnection()) {
                    it.remove()
                    return packet
                }
            }
            notEmpty.awaitUninterruptibly()
        }
    }

    /**
     * Packet Processor Task that will execute packet with respecting rules: - 1 packet / client at one time. - execute packets in received order.
     * @author -Nemesiss-
     */
    private inner class PacketProcessorTask(val name: String) : KtxRunnable {

        var ownJob: Job? = null

        override suspend fun run() {
            var packet: BaseInputPacket<T>? = null
            while (true) {
                lock.lock()
                try {
                    packet?.connection?.unlockConnection()
                    if (!ownJob!!.isActive) return
                    packet = getFirstAvailable()
                } finally {
                    lock.unlock()
                }
                scope.launch { packet?.run() }
            }
        }
    }

    /**
     * Checking if PacketProcessor is busy or idle and increasing / reducing numbers of threads.
     * @author -Nemesiss-
     */
    private inner class CheckerTask : KtxRunnable {

        /**
         * How often CheckerTask should do check.
         */
        private val sleepTime = 60 * 1000L

        /**
         * Number of packets waiting for execution on last check.
         */
        private var lastSize = 0

        override suspend fun run() {

            /* Sleep for some time */
            delay(sleepTime)

            /* Number of packets waiting for execution */
            val packetsToExecute: Int = packets.size
            if (packetsToExecute < lastSize && packetsToExecute < threadKillThreshold) {
                // too much threads
                killThread()
            } else if (packetsToExecute > lastSize && packetsToExecute > threadSpawnThreshold) {
                // too small amount of threads
                if (!newJob() && packetsToExecute >= threadSpawnThreshold * 3) {
                    log.info("Lagg detected! [$packetsToExecute client packets are waiting for execution]. You should consider increasing PacketProcessor maxThreads or hardware upgrade.")
                }
            }
            lastSize = packetsToExecute
        }

    }

}