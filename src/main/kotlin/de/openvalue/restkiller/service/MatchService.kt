package de.openvalue.restkiller.service

import de.openvalue.restkiller.dto.Status
import org.springframework.stereotype.Service
import kotlin.random.Random.Default.nextInt

@Service
class MatchService {

    val playerPositions = mutableMapOf<String, Pair<Int, Int>>()
    val playerKilled = mutableMapOf<String, String>()

    private final val template = arrayOf(
        //            p1                                                         p2
        arrayOf("▉", "▉", "▉", "▉", "▉", "▉", "▉", "▉", "▉", "▉", "▉", "▉", "▉", "▉", "▉"),
        arrayOf("▉", "x", "x", " ", " ", " ", " ", " ", " ", " ", " ", " ", "x", "x", "▉"),
        arrayOf("▉", "x", "▉", " ", "▉", " ", "▉", " ", "▉", " ", "▉", " ", "▉", "x", "▉"),
        arrayOf("▉", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", "▉"),
        arrayOf("▉", " ", "▉", " ", "▉", " ", "▉", " ", "▉", " ", "▉", " ", "▉", " ", "▉"),
        arrayOf("▉", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", "▉"),
        arrayOf("▉", " ", "▉", " ", "▉", " ", "▉", " ", "▉", " ", "▉", " ", "▉", " ", "▉"),
        arrayOf("▉", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", "▉"),
        arrayOf("▉", " ", "▉", " ", "▉", " ", "▉", " ", "▉", " ", "▉", " ", "▉", " ", "▉"),
        arrayOf("▉", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", "▉"),
        arrayOf("▉", " ", "▉", " ", "▉", " ", "▉", " ", "▉", " ", "▉", " ", "▉", " ", "▉"),
        arrayOf("▉", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", "▉"),
        arrayOf("▉", "x", "▉", " ", "▉", " ", "▉", " ", "▉", " ", "▉", " ", "▉", "x", "▉"),
        arrayOf("▉", "x", "x", " ", " ", " ", " ", " ", " ", " ", " ", " ", "x", "x", "▉"),
        arrayOf("▉", "▉", "▉", "▉", "▉", "▉", "▉", "▉", "▉", "▉", "▉", "▉", "▉", "▉", "▉"),
        //            p3                                                         p4
    )

    lateinit var level: Array<Array<String>>
    var started = false

    init {
        generateLevel()
    }

    final fun generateLevel() {
        started = false
        playerPositions.clear()
        playerKilled.clear()
        level = template.map { line -> line.map { it }.toTypedArray() }.toTypedArray()
        template.indices.forEach { row ->
            template.indices.forEach { col ->
                level[row][col] =
                    when (level[row][col]) {
                        "▉" -> "▉"
                        "x" -> " "
                        else -> if (nextInt(0, 100) <= 70) "#" else " "
                    }
            }
        }
    }

    fun getLevelMap() = level
        .map { line -> line.joinToString("") { it } }
        .joinToString("\n") { it }z

    fun enterMap(player: String) {
        if (started) throw IllegalStateException("cannot enter the game, it's already started!")
        if (playerPositions.containsKey(player)) return

        val coordinate = when (playerPositions.size) {
            0 -> Pair(1, 1)   // top left
            1 -> Pair(1, 13)  // top right
            2 -> Pair(11, 1)  // bottom left
            3 -> Pair(11, 13) // bottom right
            else -> throw IllegalStateException("cannot have a match with more than 4 players!")
        }

        playerPositions[player] = coordinate
        level[coordinate.first][coordinate.second] = player
    }

    fun move(player: String, direction: String) {
        allowPlayerToDoSomething(player)

        val target = getTarget(player, direction)
        when (level[target.first][target.second]) {
            " " -> {
                // change current position to be empty
                val coordinate = coordinate(player)
                level[coordinate.first][coordinate.second] = " "
                // change target position to be another player
                playerPositions[player] = target
                level[target.first][target.second] = player
            }
            "▉" -> throw IllegalArgumentException("Watch out! There is a hard wall!")
            "#" -> throw IllegalArgumentException("Watch out! There is a soft wall!")
            else -> throw IllegalArgumentException("Watch out! There is a player!")
        }

        if (!started) started = true
    }

    fun delete(player: String, direction: String) {
        allowPlayerToDoSomething(player)

        val target = getTarget(player, direction)
        when (level[target.first][target.second]) {
            " " -> throw IllegalArgumentException("But.. There is nothing to delete there!")
            "▉" -> throw IllegalArgumentException("Watch out! It's not possible to delete a hard wall!")
            "#" -> level[target.first][target.second] = " "
            else -> { // a player was deleted!
                val targetPlayer = level[target.first][target.second]
                playerKilled[targetPlayer] = player
                level[target.first][target.second] = " "
            }
        }
    }

    fun isPlayerWinner(player: String): Boolean {
        val deletedPlayers = playerKilled.keys
        val onlinePlayers = playerPositions.keys - deletedPlayers
        return onlinePlayers.size == 1 && onlinePlayers.contains(player)
    }

    fun loadStatus(player: String): Status {
        val coordinate = coordinate(player)
        val n = getTarget(player, "n")
        val e = getTarget(player, "e")
        val s = getTarget(player, "s")
        val w = getTarget(player, "w")
        return Status(
            player,
            x = coordinate.first,
            y = coordinate.second,
            n = level[n.first][n.second],
            e = level[e.first][e.second],
            s = level[s.first][s.second],
            w = level[w.first][w.second],
        )
    }

    /**
     * If a player is not killed and not positioned, its not playing this match at all!
     */
    private fun allowPlayerToDoSomething(player: String) {
        if (playerKilled.containsKey(player))
            throw IllegalStateException("You have been deleted by ${playerKilled[player]}, cannot play anymore!")
        if (!started && playerPositions.size < 2)
            throw IllegalArgumentException("Cannot play a game alone! Wait for others to join first!")
        if (!playerPositions.containsKey(player))
            throw IllegalArgumentException("You are not playing in this match!")
    }

    private fun getTarget(player: String, direction: String): Pair<Int, Int> {
        val coordinate = coordinate(player)
        val x = coordinate.first
        val y = coordinate.second
        return when (direction) {
            "n" -> Pair(x - 1, y) // row above, same col
            "e" -> Pair(x, y + 1) // same row, col on the right
            "s" -> Pair(x + 1, y) // row below, same col
            "w" -> Pair(x, y - 1) // same row, col on the left
            else -> throw IllegalArgumentException("Direction '$direction' does not exist.")
        }
    }

    private fun coordinate(player: String): Pair<Int, Int> =
        playerPositions[player] ?: throw IllegalArgumentException("Player '$player' is not in this match.")

}
