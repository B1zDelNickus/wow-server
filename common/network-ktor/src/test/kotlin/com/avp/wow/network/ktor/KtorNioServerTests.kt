package com.avp.wow.network.ktor

import com.avp.wow.network.ServerConfig
import com.avp.wow.network.helpers.SimpleTcpClient
import com.avp.wow.network.ktor.login.client.LoginConnectionFactory
import com.avp.wow.network.ncrypt.KeyGen
import io.kotlintest.specs.StringSpec
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import java.nio.ByteBuffer
import java.nio.ByteOrder

@KtorExperimentalAPI
class KtorNioServerTests : StringSpec({

    "test" {

        KeyGen.init()

        val host = "127.0.0.1"
        val port = 2323

        val loginServerConfig = KtorServerConfig(
            hostName = host,
            port = port,
            connectionName = "Test Login Connection",
            factory = LoginConnectionFactory()
        )

        val client = SimpleTcpClient(host, port, true, Dispatchers.IO)

        val server = KtorNioServer(
            serverConfigs = listOf(
                loginServerConfig
            ),
            context = Dispatchers.IO
        )

        server.connect()

        delay(1000)

        client.apply {
            connect()
            executeSequence {
                //sendPkt(CM
                // _TEST_FAST::class.java)

                apply {
                    val buf = ByteBuffer.allocate(8192 * 2)
                    buf.order(ByteOrder.BIG_ENDIAN)

                    buf.putShort(0.toShort())
                    buf.put(0x98.toByte())
                    buf.flip()
                    buf.putShort(0.toShort())
                    val b = buf.slice()
                    val size = 3
                    buf.putShort(0, size.toShort())
                    buf.position(0).limit(size)

                    output.writeAvailable(buf)
                    log.info { "CM_TEST_SLOW were sent to server" }
                }

                apply {
                    val buf = ByteBuffer.allocate(8192 * 2)
                    buf.order(ByteOrder.BIG_ENDIAN)

                    buf.putShort(0.toShort())
                    buf.put(0x99.toByte())
                    buf.flip()
                    buf.putShort(0.toShort())
                    val b = buf.slice()
                    val size = 3
                    buf.putShort(0, size.toShort())
                    buf.position(0).limit(size)

                    output.writeAvailable(buf)
                    log.info { "CM_TEST_FAST were sent to server" }
                }

            }
        }

        /*client.send("test")
        delay(100)
        client.send("test 2")*/

        /*val client = aSocket(ActorSelectorManager(Dispatchers.IO))
            .tcp()
            .connect(InetSocketAddress("127.0.0.1", 2323))
        val output = client.openWriteChannel(autoFlush = true)
        //output.writeStringUtf8("hello\r\n")
        //output.writeStringUtf8("world\r\n")
        //output.writeStringUtf8("exit\r\n")
        val input = client.openReadChannel()
        input.readUTF8Line().also { println("Received: $it") }*/

        delay(15_000)

        server.shutdown()

    }

}) {



}