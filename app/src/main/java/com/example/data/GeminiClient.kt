package com.example.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import com.example.BuildConfig

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<ContentPart>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: ContentPart? = null
)

@JsonClass(generateAdapter = true)
data class ContentPart(
    val parts: List<PartText>
)

@JsonClass(generateAdapter = true)
data class PartText(
    val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    val temperature: Float? = null,
    val thinkingConfig: ThinkingConfig? = null
)

@JsonClass(generateAdapter = true)
data class ThinkingConfig(
    val thinkingLevel: String
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<CandidateText>?
)

@JsonClass(generateAdapter = true)
data class CandidateText(
    val content: ContentPart?
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.1-pro-preview:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val apiService: GeminiApiService by lazy {
        retrofit.create(GeminiApiService::class.java)
    }

    suspend fun generateResponse(prompt: String, systemPrompt: String? = null): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return "Vui lòng cấu hình GEMINI_API_KEY trong tab Secrets trước khi sử dụng AI."
        }

        val request = GeminiRequest(
            contents = listOf(ContentPart(parts = listOf(PartText(text = prompt)))),
            generationConfig = GenerationConfig(
                temperature = 0.7f,
                thinkingConfig = ThinkingConfig(thinkingLevel = "high")
            ),
            systemInstruction = systemPrompt?.let {
                ContentPart(parts = listOf(PartText(text = it)))
            }
        )

        return try {
            val response = apiService.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: "AI không trả về phản hồi nào."
        } catch (e: Exception) {
            "Lỗi gọi Gemini API: ${e.localizedMessage ?: e.message}"
        }
    }
}
