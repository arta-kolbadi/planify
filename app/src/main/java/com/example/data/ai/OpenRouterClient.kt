package com.example.data.ai

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

data class OpenRouterMessage(
    val role: String,
    val content: String,
    val imageBase64DataUrl: String? = null
)

class OpenRouterClient {
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun testConnection(apiKey: String, baseUrl: String): Result<Int> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("API key is empty"))
        }
        try {
            val cleanBase = baseUrl.trim().trimEnd('/')
            val request = Request.Builder()
                .url("$cleanBase/models")
                .addHeader("Authorization", "Bearer ${apiKey.trim()}")
                .addHeader("HTTP-Referer", "https://planify.local")
                .addHeader("X-Title", "PLANIFY Android")
                .get()
                .build()

            httpClient.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    val msg = parseErrorMessage(body, response.code)
                    return@withContext Result.failure(IllegalStateException(msg))
                }
                val count = runCatching {
                    JSONObject(body).optJSONArray("data")?.length() ?: 0
                }.getOrDefault(0)
                Result.success(count)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun streamChatCompletion(
        apiKey: String,
        baseUrl: String,
        model: String,
        messages: List<OpenRouterMessage>,
        temperature: Float,
        maxTokens: Int
    ): Flow<String> = flow {
        if (apiKey.isBlank()) {
            throw IllegalStateException("OpenRouter API key is missing.")
        }
        val cleanBase = baseUrl.trim().trimEnd('/')
        val payload = JSONObject().apply {
            put("model", model.trim().ifBlank { "openai/gpt-4o-mini" })
            put("stream", true)
            put("temperature", temperature.toDouble())
            put("max_tokens", maxTokens)
            val msgArray = JSONArray()
            for (msg in messages) {
                val mObj = JSONObject()
                mObj.put("role", msg.role)
                if (msg.imageBase64DataUrl != null && msg.role == "user") {
                    val parts = JSONArray().apply {
                        put(JSONObject().put("type", "text").put("text", msg.content))
                        put(
                            JSONObject().put("type", "image_url").put(
                                "image_url",
                                JSONObject().put("url", msg.imageBase64DataUrl)
                            )
                        )
                    }
                    mObj.put("content", parts)
                } else {
                    mObj.put("content", msg.content)
                }
                msgArray.put(mObj)
            }
            put("messages", msgArray)
        }

        val request = Request.Builder()
            .url("$cleanBase/chat/completions")
            .addHeader("Authorization", "Bearer ${apiKey.trim()}")
            .addHeader("Content-Type", "application/json")
            .addHeader("HTTP-Referer", "https://planify.local")
            .addHeader("X-Title", "PLANIFY Android")
            .post(payload.toString().toRequestBody(jsonMediaType))
            .build()

        val call = httpClient.newCall(request)
        try {
            call.execute().use { response ->
                if (!response.isSuccessful) {
                    val errBody = response.body?.string().orEmpty()
                    throw IllegalStateException(parseErrorMessage(errBody, response.code))
                }
                val stream = response.body?.byteStream()
                    ?: throw IllegalStateException("Empty response stream")
                BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { reader ->
                    while (true) {
                        val line = reader.readLine() ?: break
                        if (!line.startsWith("data:")) continue
                        val data = line.removePrefix("data:").trim()
                        if (data == "[DONE]") break
                        if (data.isBlank()) continue
                        val chunkJson = runCatching { JSONObject(data) }.getOrNull() ?: continue
                        val choices = chunkJson.optJSONArray("choices") ?: continue
                        if (choices.length() > 0) {
                            val choice = choices.getJSONObject(0)
                            val delta = choice.optJSONObject("delta")
                            val piece = delta?.optString("content", "")
                                ?: choice.optJSONObject("message")?.optString("content", "")
                                ?: ""
                            if (piece.isNotEmpty()) {
                                emit(piece)
                            }
                        }
                    }
                }
            }
        } catch (e: CancellationException) {
            call.cancel()
            throw e
        }
    }.flowOn(Dispatchers.IO)

    private fun parseErrorMessage(body: String, code: Int): String {
        val parsed = runCatching {
            val obj = JSONObject(body)
            obj.optJSONObject("error")?.optString("message")
                ?.takeIf { it.isNotBlank() }
                ?: obj.optString("message").takeIf { it.isNotBlank() }
        }.getOrNull()
        return parsed ?: "HTTP $code from OpenRouter"
    }
}
