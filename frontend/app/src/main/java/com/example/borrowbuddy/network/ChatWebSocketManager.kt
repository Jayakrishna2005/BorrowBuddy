package com.example.borrowbuddy.network

import okhttp3.*
import okio.ByteString
import org.json.JSONObject
import android.util.Log

class ChatWebSocketManager(private val baseUrl: String, private val requestId: String) {
    private var client: OkHttpClient = OkHttpClient()
    private var webSocket: WebSocket? = null
    
    // Convert http(s) to ws(s)
    private val wsUrl: String = run {
        val protocol = if (baseUrl.startsWith("https")) "wss" else "ws"
        val domain = baseUrl.substringAfter("://").removeSuffix("/")
        "$protocol://$domain/ws/chat/$requestId/"
    }

    fun connect(onMessageReceived: (JSONObject) -> Unit, onStatusChanged: (Boolean) -> Unit) {
        val request = Request.Builder().url(wsUrl).build()
        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("ChatWS", "Connected")
                onStatusChanged(true)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("ChatWS", "Message received: $text")
                onMessageReceived(JSONObject(text))
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
                Log.d("ChatWS", "Closing: $code / $reason")
                onStatusChanged(false)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("ChatWS", "Error: ${t.message}")
                onStatusChanged(false)
            }
        }
        webSocket = client.newWebSocket(request, listener)
    }

    fun sendMessage(message: String, senderId: String, receiverId: String?) {
        val json = JSONObject()
        json.put("type", "chat_message")
        json.put("message", message)
        json.put("sender_id", senderId)
        if (receiverId != null) {
            json.put("receiver_id", receiverId)
        }
        webSocket?.send(json.toString())
    }

    fun markSeen() {
        val json = JSONObject()
        json.put("type", "mark_seen")
        webSocket?.send(json.toString())
    }

    fun disconnect() {
        webSocket?.close(1000, "User disconnected")
    }
}
