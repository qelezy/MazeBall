package com.example.mazeball.server

import com.example.mazeball.shared.LeaderboardEntry
import java.sql.DriverManager
import java.util.concurrent.ConcurrentHashMap

object Database {
    private const val DB_URL = "jdbc:sqlite:mazeball.db"
    
    fun init() {
        DriverManager.getConnection(DB_URL).use { conn ->
            conn.createStatement().use { stmt ->
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS leaderboard_entries (
                        level_id INTEGER NOT NULL,
                        device_id TEXT NOT NULL,
                        player_name TEXT NOT NULL,
                        time_millis INTEGER NOT NULL,
                        PRIMARY KEY (level_id, device_id)
                    )
                """.trimIndent())
                
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS player_names (
                        device_id TEXT PRIMARY KEY,
                        player_name TEXT NOT NULL
                    )
                """.trimIndent())
            }
        }
    }
    
    fun loadLeaderboards(): ConcurrentHashMap<Int, MutableList<LeaderboardEntry>> {
        val leaderboards = ConcurrentHashMap<Int, MutableList<LeaderboardEntry>>()
        DriverManager.getConnection(DB_URL).use { conn ->
            conn.createStatement().use { stmt ->
                val rs = stmt.executeQuery("SELECT level_id, device_id, player_name, time_millis FROM leaderboard_entries")
                while (rs.next()) {
                    val levelId = rs.getInt("level_id")
                    val deviceId = rs.getString("device_id")
                    val playerName = rs.getString("player_name")
                    val timeMillis = rs.getLong("time_millis")
                    leaderboards.getOrPut(levelId) { mutableListOf() }
                        .add(LeaderboardEntry(deviceId, playerName, timeMillis))
                }
            }
        }
        return leaderboards
    }
    
    fun loadPlayerNames(): ConcurrentHashMap<String, String> {
        val playerNames = ConcurrentHashMap<String, String>()
        DriverManager.getConnection(DB_URL).use { conn ->
            conn.createStatement().use { stmt ->
                val rs = stmt.executeQuery("SELECT device_id, player_name FROM player_names")
                while (rs.next()) {
                    playerNames[rs.getString("device_id")] = rs.getString("player_name")
                }
            }
        }
        return playerNames
    }
    
    fun saveLeaderboardEntry(levelId: Int, entry: LeaderboardEntry) {
        DriverManager.getConnection(DB_URL).use { conn ->
            conn.prepareStatement(
                "INSERT OR REPLACE INTO leaderboard_entries (level_id, device_id, player_name, time_millis) VALUES (?, ?, ?, ?)"
            ).use { stmt ->
                stmt.setInt(1, levelId)
                stmt.setString(2, entry.deviceId)
                stmt.setString(3, entry.playerName)
                stmt.setLong(4, entry.timeMillis)
                stmt.executeUpdate()
            }
        }
    }
    
    fun updateLeaderboardEntry(levelId: Int, entry: LeaderboardEntry) {
        DriverManager.getConnection(DB_URL).use { conn ->
            conn.prepareStatement(
                "UPDATE leaderboard_entries SET time_millis = ?, player_name = ? WHERE level_id = ? AND device_id = ?"
            ).use { stmt ->
                stmt.setLong(1, entry.timeMillis)
                stmt.setString(2, entry.playerName)
                stmt.setInt(3, levelId)
                stmt.setString(4, entry.deviceId)
                stmt.executeUpdate()
            }
        }
    }
    
    fun savePlayerName(deviceId: String, playerName: String) {
        DriverManager.getConnection(DB_URL).use { conn ->
            conn.prepareStatement(
                "INSERT OR REPLACE INTO player_names (device_id, player_name) VALUES (?, ?)"
            ).use { stmt ->
                stmt.setString(1, deviceId)
                stmt.setString(2, playerName)
                stmt.executeUpdate()
            }
        }
    }
    
    fun updateLeaderboardEntryPlayerName(deviceId: String, playerName: String) {
        DriverManager.getConnection(DB_URL).use { conn ->
            conn.prepareStatement(
                "UPDATE leaderboard_entries SET player_name = ? WHERE device_id = ?"
            ).use { stmt ->
                stmt.setString(1, playerName)
                stmt.setString(2, deviceId)
                stmt.executeUpdate()
            }
        }
    }
}

