package com.avp.wow.network

import com.avp.wow.network.todo_move.KeyGen
import com.avp.wow.network.todo_move.LoginConnectionFactory
import io.kotlintest.specs.StringSpec
import io.ktor.network.selector.ActorSelectorManager
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.util.KtorExperimentalAPI
import io.ktor.utils.io.readRemaining
import io.ktor.utils.io.readUTF8Line
import io.ktor.utils.io.writeStringUtf8
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import java.net.InetSocketAddress

@KtorExperimentalAPI
class NioServerTests : StringSpec({

    "test" {

        KeyGen.init()

        val host = "127.0.0.1"
        val port = 2323

        val loginServerConfig = ServerConfig(
            hostName = host,
            port = port,
            connectionName = "Test Login Connection",
            factory = LoginConnectionFactory()
        )

        //val client = SimpleTcpClient(host, port, true, Dispatchers.IO)

        val server = NioServer(
            serverConfigs = listOf(
                loginServerConfig
            ),
            context = Dispatchers.IO
        )

        server.connect()

        //delay(1000)

        /*client.send("test")
        delay(100)
        client.send("test 2")*/

        val client = aSocket(ActorSelectorManager(Dispatchers.IO))
            .tcp()
            .connect(InetSocketAddress("127.0.0.1", 2323))
        val output = client.openWriteChannel(autoFlush = true)
        //output.writeStringUtf8("hello\r\n")
        //output.writeStringUtf8("world\r\n")
        //output.writeStringUtf8("exit\r\n")
        val input = client.openReadChannel()
        input.readUTF8Line().also { println("Received: $it") }

        delay(2000)

        server.shutdown()

    }

}) {



}