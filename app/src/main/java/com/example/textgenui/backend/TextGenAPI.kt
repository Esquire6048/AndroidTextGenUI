package com.example.textgenui.backend

import android.util.Log
import com.launchdarkly.eventsource.CommentEvent
import com.launchdarkly.eventsource.ConnectStrategy
import com.launchdarkly.eventsource.EventSource
import com.launchdarkly.eventsource.MessageEvent
import com.launchdarkly.eventsource.StartedEvent
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URI

/**
 * TextGen API docs are at http://127.0.0.1:5000/docs#/
 */
class TextgenAPI(val host: String) {
    private val TAG = this::class.simpleName


    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    fun loadModel(modelName: String, instructionTemplate: String) {
        println("Loading model $modelName...")
        val requestBody = ModelRequest(
            model_name = modelName,
            args = ModelLoadArgs(loader = "ExLlamav2_HF"),
            settings = ModelSettings(instruction_template = instructionTemplate)
        )
        HTTPUtils.sendPOSTRequest(uri = URI("$host/v1/internal/model/load"), requestBodyJson = json.encodeToString(requestBody))
    }

    fun getModelInfoString(): String {
        val response = HTTPUtils.sendGETRequest(uri = URI("$host/v1/internal/model/info"))
        val responseJSONElement = json.parseToJsonElement(response)
        val modelName = responseJSONElement.jsonObject["model_name"]!!
        //val loras = responseJSONElement.jsonObject["lora_names"]!!.jsonArray
        return modelName.jsonPrimitive.content //, Lora(s): $loras"
    }

    fun sendChatMessageStreaming(
        history: List<MsgItem>,
        userBio: String?,
        context: String?,
        temperature: Float,
        onNewMessageChunk: (String) -> Unit
    ) {
        val userName = "V"
        val characterName = "Phone"
        val genreq = TextGenRequestOpenAIAPI(
            messages = history,
            mode = "chat-instruct", // Valid options: 'chat', 'chat-instruct', 'instruct'
            character = null,
            chat_instruct_command = "Continue the chat dialogue below. Use verbal dialogue only without narration. " +
                    "Write a single reply for the character \"<|character|>\".\n\n<|prompt|>",
            name1 = userName,
            name2 = characterName,
            max_tokens = 512,
            min_p = 0.05,
            temperature = temperature,
            top_k = 50,
            repetition_penalty = 1f,
            mirostat_mode = 0,
            mirostat_tau = 5.0,
            mirostat_eta = 0.1,
            user_bio = userBio,
            context = context,
            stream = true
        )

        val eventSourceSse: EventSource = EventSource.Builder(
            ConnectStrategy
                .http(URI.create("$host/v1/chat/completions"))
                .headers(HTTPUtils.myHeaders)
                .methodAndBody("POST", json.encodeToString(genreq).toRequestBody("application/json".toMediaTypeOrNull()))
        ).build()
        eventSourceSse.start()
        for (ev in eventSourceSse.anyEvents()) {
            //println("event: ${ev.javaClass.name}")
            when (ev) {
                is CommentEvent -> Log.i(TAG, "CommentEvent: ${ev.text}") // TODO show somewhere if meaningful
                is StartedEvent -> Log.i(TAG, "StartedEvent: $ev") // TODO show somewhere if meaningful
                is MessageEvent -> {
                    val e = ev as MessageEvent
                    //println("event data: " + e.data)
                    val modelResponse = json.decodeFromString<TextGenModelResponseStreaming>(e.data)
                    check(modelResponse.choices.size == 1)
                    onNewMessageChunk(modelResponse.choices[0].delta.content.trimEnd())
                }
            }
        }
        eventSourceSse.close()
    }
}