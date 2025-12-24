package com.example.mazeball.game

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect

class GameEngine(context: Context, private val maze: RenderableMaze) : SensorEventListener {

    var gameState by mutableStateOf(GameState(ballPosition = maze.start))
        private set

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var velocity = Offset.Zero
    private var lastUpdateTime = 0L

    init {
        resetGame()
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)
    }

    fun release() {
        sensorManager.unregisterListener(this)
    }

    fun resetGame() {
        gameState = GameState(ballPosition = maze.start, scrollOffset = Offset.Zero)
        velocity = Offset.Zero
        lastUpdateTime = System.currentTimeMillis()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (gameState.gameWon) return

        event?.let { e ->
            val now = System.currentTimeMillis()
            if (now - lastUpdateTime < 16) return

            val acceleration = Offset(-e.values[0] * 0.6f, e.values[1] * 0.6f)
            velocity += acceleration
            velocity *= 0.95f

            var newX = gameState.ballPosition.x + velocity.x
            var newY = gameState.ballPosition.y + velocity.y

            val bounceFactor = 0.8f
            var newVelX = velocity.x
            var newVelY = velocity.y

            for (wall in maze.walls) {
                if (Rect(newX - BALL_RADIUS, gameState.ballPosition.y - BALL_RADIUS, newX + BALL_RADIUS, gameState.ballPosition.y + BALL_RADIUS).overlaps(wall)) {
                    newX = if (velocity.x > 0) wall.left - BALL_RADIUS else wall.right + BALL_RADIUS
                    newVelX = -velocity.x * bounceFactor
                    break
                }
            }

            for (wall in maze.walls) {
                if (Rect(gameState.ballPosition.x - BALL_RADIUS, newY - BALL_RADIUS, gameState.ballPosition.x + BALL_RADIUS, newY + BALL_RADIUS).overlaps(wall)) {
                    newY = if (velocity.y > 0) wall.top - BALL_RADIUS else wall.bottom + BALL_RADIUS
                    newVelY = -velocity.y * bounceFactor
                    break
                }
            }
            
            velocity = Offset(newVelX, newVelY)
            val newPosition = Offset(newX, newY)
            
            val scrollDelta = velocity * 0.8f
            val newScrollOffset = gameState.scrollOffset + scrollDelta

            val gameWon = maze.exit.contains(newPosition)

            gameState = gameState.copy(
                ballPosition = newPosition,
                gameWon = gameWon,
                elapsedTime = if(gameWon) gameState.elapsedTime else (gameState.elapsedTime + (now - lastUpdateTime)),
                scrollOffset = newScrollOffset
            )

            lastUpdateTime = now
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    companion object {
        const val BALL_RADIUS = 38f
    }
}

data class GameState(
    val ballPosition: Offset = Offset.Zero,
    val gameWon: Boolean = false,
    val elapsedTime: Long = 0L,
    val scrollOffset: Offset = Offset.Zero
)
