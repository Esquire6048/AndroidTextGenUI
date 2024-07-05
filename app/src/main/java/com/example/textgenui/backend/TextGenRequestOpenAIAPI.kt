package com.example.textgenui.backend

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class TextGenRequestOpenAIAPI(
    val messages: List<MsgItem>,
    val mode: String,
    val user_bio: String?,
    val context: String?,
    val character: String?,
    val chat_instruct_command: String,
    val name1: String,
    val name2: String,
    val max_tokens: Int,
    val mirostat_mode: Int,
    val mirostat_tau: Double,
    val mirostat_eta: Double,
    val min_p: Double,
    val temperature: Float,
    val top_k: Int,
    val repetition_penalty: Float,
    val stream: Boolean
    //val seed: Int,
)

@Serializable
data class MsgItem(val role: String, val content: String)

@Serializable
data class TextGenModelResponse(
    val id: String,
    @SerialName("object")
    val obj: String,
    val created: Long,
    val model: String,
    val choices: List<Choice>,
    val usage: Usage
)

@Serializable
data class TextGenModelResponseStreaming(
    val id: String,
    @SerialName("object")
    val obj: String,
    val created: Long,
    val model: String,
    val choices: List<ChoiceStreaming>
)

@Serializable
data class Usage(
    val prompt_tokens: Int,
    val completion_tokens: Int,
    val total_tokens: Int
)

@Serializable
data class Choice(
    val index: Int,
    val finish_reason: String,
    val message: MsgItem
)

@Serializable
data class ChoiceStreaming(
    val index: Int,
    val finish_reason: String?,
    val delta: MsgItem
)