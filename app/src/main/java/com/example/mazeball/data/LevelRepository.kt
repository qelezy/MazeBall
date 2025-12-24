package com.example.mazeball.data

import com.example.mazeball.shared.SyncRequest
import com.example.mazeball.shared.ApiLevel
import com.example.mazeball.shared.LeaderboardEntry
import com.example.mazeball.shared.SubmitRequest
import com.example.mazeball.shared.UpdateNicknameRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager


class NicknameTakenException : Exception("Этот никнейм уже занят.")

class LevelRepository(val userPreferencesRepository: UserPreferencesRepository) {

    private var cachedLeaderboards: Map<Int, List<LeaderboardEntry>>? = null
    private var lastFetchTimestamp: Long = 0
    private val cacheDurationMs = 60 * 1000

    private fun updateCache(newLeaderboards: Map<Int, List<LeaderboardEntry>>) {
        cachedLeaderboards = newLeaderboards
        lastFetchTimestamp = System.currentTimeMillis()
    }

    private val client = HttpClient(CIO) {
        expectSuccess = true
        engine {
            https {
                trustManager = object : X509TrustManager {
                    override fun checkClientTrusted(p0: Array<out X509Certificate>?, p1: String?) = Unit
                    override fun checkServerTrusted(p0: Array<out X509Certificate>?, p1: String?) = Unit
                    override fun getAcceptedIssuers(): Array<X509Certificate>? = null
                }
            }
        }
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        defaultRequest {
            header("bypass-tunnel-reminder", "true")
        }
    }

    private suspend fun getBaseUrl(): String {
        return userPreferencesRepository.userPreferencesFlow.first().serverUrl
    }

    suspend fun getLevels(): List<ApiLevel> = withContext(Dispatchers.IO) {
        LocalLevels.allGameLevels
    }

    suspend fun getAllLeaderboards(): Map<Int, List<LeaderboardEntry>> {
        val now = System.currentTimeMillis()
        if (cachedLeaderboards != null && now - lastFetchTimestamp < cacheDurationMs) {
            return cachedLeaderboards!!
        }
        val baseUrl = getBaseUrl()
        if (baseUrl.isEmpty()) return emptyMap()
        val newLeaderboards = client.get("$baseUrl/leaderboard/all").body<Map<Int, List<LeaderboardEntry>>>()
        updateCache(newLeaderboards)
        return newLeaderboards
    }

    suspend fun updateUserNicknameAndSync(newNickname: String): Map<Int, List<LeaderboardEntry>> {
        val baseUrl = getBaseUrl()
        if (baseUrl.isEmpty()) {
            return cachedLeaderboards ?: emptyMap()
        }
        try {
            val deviceId = userPreferencesRepository.userPreferencesFlow.first().deviceId
            client.post("$baseUrl/user/nickname") {
                contentType(ContentType.Application.Json)
                setBody(UpdateNicknameRequest(deviceId, newNickname))
            }
            
            userPreferencesRepository.updateNickname(newNickname)

            return syncAllBestTimes()
        } catch (e: ClientRequestException) {
            if (e.response.status == HttpStatusCode.Conflict) {
                throw NicknameTakenException()
            }
            throw e
        }
    }

    suspend fun syncAllBestTimes(): Map<Int, List<LeaderboardEntry>> {
        val baseUrl = getBaseUrl()
        if (baseUrl.isEmpty()) throw Exception("URL сервера не настроен.")

        val deviceId = userPreferencesRepository.userPreferencesFlow.first().deviceId
        val localScores = userPreferencesRepository.getAllBestTimes()
            .map { (levelId, time) -> SubmitRequest(levelId, deviceId, time) }

        val newLeaderboards = client.post("$baseUrl/leaderboard/sync") {
            contentType(ContentType.Application.Json)
            setBody(SyncRequest(deviceId, localScores))
        }.body<Map<Int, List<LeaderboardEntry>>>()

        updateCache(newLeaderboards)
        return newLeaderboards
    }
}
