package com.badlogic.drop

import com.badlogic.gdx.Gdx
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.Socket

class TcpReceiver(
    private val socket: Socket,
    private val queue: ThreadSafeQueue<String>
) : Thread() {
    override fun run() {
        try {
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
            println("Socket input stream opened")
            while (!socket.isClosed) {
                val message = reader.readLine() ?: break
                Gdx.app.log("Network", "Message reading: $message")
                queue.enqueue(message)
                println("Received: $message")
            }
        } catch (e: Exception) {
            println("Error received: ${e.message}")
            e.printStackTrace()
        } finally {
            socket.close()
        }
    }

}
