package com.avp.wow.network.client

/*
@KtorExperimentalAPI
class SimpleWowClient(
    private val host: String,
    private val port: Int
) {

    private val log = KotlinLogging.logger(this::class.java.name)

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val cryptEngine = CryptEngine()

    fun connect() {
        scope.launch {

            val socket = aSocket(ActorSelectorManager(scope.coroutineContext))
                .tcp()
                .connect(hostname = host, port = port)

            log.info { "Connected to ${socket.remoteAddress}" }

            val input = socket.openReadChannel()
            val output = socket.openWriteChannel(autoFlush = true)

            val readBuffer = ByteBuffer.allocate(9000 * 2)
                .apply {
                    order(ByteOrder.BIG_ENDIAN)
                }!!

            scope.launch {
                var isActive = true
                while (isActive) {

                    when (input.readAvailable(readBuffer)) {
                        -1 -> {
                            try {
                                socket.close()
                                socket.dispose()
                                log.info { "Disconnected from server" }
                            } catch (e: Exception) {}
                            isActive = false
                        }
                        0 -> Unit
                        else -> {
                            //log.debug { "Received packet with len: ${rb.remaining()}" }

                            readBuffer.flip()

                            while (readBuffer.remaining() > 2 && readBuffer.remaining() >= readBuffer.getShort(
                                    readBuffer.position())) {
                                */
/**
                                 * got full message
                                 *//*

                                if (!parse(readBuffer)) {
                                    //nioServer.closeConnectionImpl(this)
                                    break
                                }
                            }

                            when {
                                readBuffer.hasRemaining() -> readBuffer.compact()
                                else -> readBuffer.clear()
                            }

                            readBuffer.clear()
                        }
                    }

                }

            }

        }
    }

    private fun parse(buf: ByteBuffer): Boolean {
        var sz: Short = 0
        try {
            sz = buf.short
            if (sz > 1) {
                sz = (sz - 2).toShort()
            }
            val b = buf.slice().limit(sz.toInt()) as ByteBuffer
            b.order(ByteOrder.LITTLE_ENDIAN)
            */
/**
             * read message fully
             *//*

            buf.position(buf.position() + sz)
            return processData(b)
        } catch (e: IllegalArgumentException) {
            log.warn(e) { "Error on parsing input from client - account: " + this + " packet size: " + sz + " real size:" + buf.remaining() }
            return false
        }

    }

    fun processData(data: ByteBuffer): Boolean {

        if (!decrypt(data)) {
            return false
        }

        val pck = LoginServerInputPacketFactory.define(data*/
/*, this*//*
)

        */
/**
         * Execute packet only if packet exist (!= null) and read was ok.
         *//*

        if (pck != null */
/*&& pck.read()*//*
) {
            log.debug { "Received packet $pck from server" }
            //processor.executePacket(pck)
        }

        return true
    }

    private fun decrypt(buf: ByteBuffer): Boolean {
        val size = buf.remaining()
        val offset = buf.arrayOffset() + buf.position()
        val ret = cryptEngine.decrypt(buf.array(), offset, size)
            ?: throw IllegalArgumentException("Crypt Engine was not initialized properly")
        if (!ret) { log.warn { "Wrong checksum from client: $this" } }
        return ret
    }

}

@KtorExperimentalAPI
inline fun buildWowClient(
    host: String = DEFAULT_LOGIN_SERVER_HOST,
    port: Int = DEFAULT_LOGIN_SERVER_PORT,
    configure: SimpleWowClient.() -> Unit
) = SimpleWowClient(host = host, port = port)
    .apply {
        configure()
        connect()
    }*/
