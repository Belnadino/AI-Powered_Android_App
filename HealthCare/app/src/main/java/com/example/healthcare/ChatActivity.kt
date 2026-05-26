package com.example.healthcare

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

class ChatActivity : BaseActivity() {

    private val apiKey = BuildConfig.OPENAI_API_KEY
    private lateinit var editMessage: EditText
    private lateinit var btnSend: Button
    private lateinit var rvMessages: RecyclerView
    private lateinit var adapter: ChatAdapter
    private val messages = mutableListOf<ChatMessage>()
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        rvMessages = findViewById(R.id.rvMessages)
        editMessage = findViewById(R.id.editMessage)
        btnSend = findViewById(R.id.btnSend)

        adapter = ChatAdapter(messages)
        rvMessages.adapter = adapter
        rvMessages.layoutManager = LinearLayoutManager(this)

        // Initial assistant message
        addMessageToChat("I am here for you, ask me anything", sentByUser = false)

        btnSend.setOnClickListener {
            val userMessage = editMessage.text.toString().trim()
            if (userMessage.isNotEmpty()) {
                addMessageToChat(userMessage, sentByUser = true)
                editMessage.text.clear()
                sendToOpenAI(userMessage)
            }
        }
    }

    private fun addMessageToChat(message: String, sentByUser: Boolean) {
        adapter.addMessage(ChatMessage(message, sentByUser))
        rvMessages.scrollToPosition(adapter.itemCount - 1)
    }

    private fun sendToOpenAI(userMessage: String) {
        val healthPrompt = """
You are a friendly and helpful health and psychological assistant.
You can answer any health-related question, including but not limited to:
- Physical health and medical conditions (diseases, symptoms, treatments, medications)
- Mental health and psychological well-being (stress, anxiety, depression, emotions, therapy guidance)
- Nutrition and diet (meal planning, healthy eating, supplements)
- Fitness and exercise (workouts, strength, flexibility, endurance)
- Lifestyle and daily habits (sleep, relaxation, routines, preventive care)
- Sexual and reproductive health (safe practices, reproductive system, contraception)
- Chronic illnesses and long-term conditions (diabetes, heart disease, arthritis, etc.)
- First aid and emergency guidance (basic advice, when to seek medical help)
- General health advice and tips for maintaining overall wellness

If the user asks something that is not related to health, gently guide the conversation back to health topics.
Always acknowledge and respond appropriately to user feedback such as "thank you", "sorry", or compliments.
Maintain a warm, empathetic, and supportive tone in your responses.


            User question: "$userMessage"
        """.trimIndent()

        val safeMessage = JSONObject.quote(healthPrompt)
        val json = """
            {
              "model": "gpt-3.5-turbo",
              "messages": [{"role": "user", "content": $safeMessage}]
            }
        """.trimIndent()

        val requestBody = json.toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .header("Authorization", "Bearer $apiKey")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    addMessageToChat(
                        "There is a connection issue. Please check your internet connection or try again later.",
                        sentByUser = false
                    )
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: "No response"
                val aiMessage = parseAIResponse(body)
                runOnUiThread {
                    addMessageToChat(aiMessage, sentByUser = false)
                }
            }
        })
    }

    private fun parseAIResponse(responseBody: String): String {
        return try {
            val jsonObj = JSONObject(responseBody)
            if (jsonObj.has("error")) {
                jsonObj.getJSONObject("error").getString("message")
            } else {
                val choices = jsonObj.getJSONArray("choices")
                val messageObj = choices.getJSONObject(0).getJSONObject("message")
                messageObj.getString("content").trim()
            }
        } catch (e: Exception) {
            "Error parsing response"
        }
    }
}
