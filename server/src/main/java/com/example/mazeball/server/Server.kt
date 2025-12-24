package com.example.mazeball.server

import com.example.mazeball.shared.LeaderboardEntry
import com.example.mazeball.shared.SyncRequest
import com.example.mazeball.shared.UpdateNicknameRequest
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.concurrent.ConcurrentHashMap

val leaderboards = ConcurrentHashMap<Int, MutableList<LeaderboardEntry>>()
val playerNames = ConcurrentHashMap<String, String>()

fun main() {
    Database.init()

    leaderboards.putAll(Database.loadLeaderboards())
    playerNames.putAll(Database.loadPlayerNames())

    println("✅ Данные загружены из базы данных.")

    Runtime.getRuntime().addShutdownHook(Thread {
        println("\n🔌 Сервер останавливается...")
        println("✅ Данные сохранены. До свидания!")
    })

    val port = 8080
    val localIp = getLocalIpAddress()
    val host = localIp ?: "0.0.0.0"

    println("\n" + "─".repeat(80))
    println("🚀 Сервер списков лидеров MazeBall")
    if (localIp != null) {
        println("📡 Локальный IP сервера: $localIp")
    } else {
        println("⚠️ Не удалось определить локальный IP, используем 0.0.0.0")
    }
    println("✅ Сервер слушает на $host:$port")
    println("─".repeat(80) + "\n")

    embeddedServer(Netty, host = host, port = port) {
        mainModule()
    }.start(wait = true)
}

fun Application.mainModule() {
    install(ContentNegotiation) {
        json()
    }

    routing {

        get("/leaderboard/all") {
            val processed = leaderboards.mapValues { (_, list) ->
                list.filter { it.playerName.isNotBlank() }
                    .sortedBy { it.timeMillis }
            }
            call.respond(processed)
        }

        post("/leaderboard/sync") {
            val syncRequest = call.receive<SyncRequest>()
            val playerName = playerNames[syncRequest.deviceId] ?: ""

            syncRequest.scores.forEach { score ->
                val leaderboard = leaderboards.getOrPut(score.levelId) { mutableListOf() }
                val existing = leaderboard.find { it.deviceId == syncRequest.deviceId }

                if (existing == null) {
                    val entry = LeaderboardEntry(
                        syncRequest.deviceId,
                        playerName,
                        score.timeMillis
                    )
                    leaderboard.add(entry)
                    Database.saveLeaderboardEntry(score.levelId, entry)
                } else if (score.timeMillis < existing.timeMillis) {
                    existing.timeMillis = score.timeMillis
                    Database.updateLeaderboardEntry(score.levelId, existing)
                }
            }

            val processed = leaderboards.mapValues { (_, list) ->
                list.filter { it.playerName.isNotBlank() }
                    .sortedBy { it.timeMillis }
            }

            call.respond(processed)
        }

        post("/user/nickname") {
            val request = call.receive<UpdateNicknameRequest>()

            val taken = playerNames.any { (deviceId, name) ->
                name.equals(request.newNickname, ignoreCase = true)
                        && deviceId != request.deviceId
            }

            if (taken) {
                call.respond(HttpStatusCode.Conflict, "Nickname is already taken")
                return@post
            }

            playerNames[request.deviceId] = request.newNickname
            Database.savePlayerName(request.deviceId, request.newNickname)

            leaderboards.values.forEach { leaderboard ->
                leaderboard.forEach {
                    if (it.deviceId == request.deviceId) {
                        it.playerName = request.newNickname
                    }
                }
            }

            Database.updateLeaderboardEntryPlayerName(
                request.deviceId,
                request.newNickname
            )

            call.respond(HttpStatusCode.OK)
        }
    }
}

fun getLocalIpAddress(): String? {
    try {
        val interfaces = NetworkInterface.getNetworkInterfaces()?.toList() ?: return null

        val undesirableKeywords = setOf(
            "virtual", "vpn", "radmin", "switch", "hamachi",
            "docker", "loopback", "vmware", "hyper-v",
            "vbox", "bridge", "default"
        )

        val candidates = interfaces.filter { iface ->
            iface.isUp &&
            !iface.isLoopback &&
            !iface.isVirtual &&
            undesirableKeywords.none {
                iface.displayName.contains(it, ignoreCase = true)
            }
        }

        val wirelessKeywords = setOf(
            "wireless", "wi-fi", "wifi", "wlan",
            "беспровод", "wlp"
        )

        val (wireless, other) = candidates.partition { iface ->
            wirelessKeywords.any {
                iface.displayName.contains(it, ignoreCase = true)
            }
        }

        fun findIp(list: List<NetworkInterface>, type: String): String? {
            for (iface in list) {
                for (addr in iface.inetAddresses.toList()) {
                    if (
                        addr is Inet4Address &&
                        addr.isSiteLocalAddress &&
                        !addr.hostAddress.startsWith("169.254")
                    ) {
                        println("✅ Найден $type адаптер: '${iface.displayName}', IP: ${addr.hostAddress}")
                        return addr.hostAddress
                    }
                }
            }
            return null
        }

        findIp(wireless, "беспроводной")?.let { return it }
        findIp(other, "проводной")?.let { return it }

        println("⚠️ Подходящий IP-адрес не найден")

    } catch (e: Exception) {
        println("❌ Ошибка определения IP: ${e.message}")
    }
    return null
}
