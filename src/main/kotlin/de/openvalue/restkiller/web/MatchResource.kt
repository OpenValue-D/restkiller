package de.openvalue.restkiller.web

import de.openvalue.restkiller.service.MatchService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod.*
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.OK
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.MediaType.TEXT_PLAIN_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.User
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/")
class MatchResource {

    @Autowired
    lateinit var matchService: MatchService

    @RequestMapping(method = [RequestMethod.OPTIONS], produces = [APPLICATION_JSON_VALUE])
    fun getInfo(): ResponseEntity<Any> {
        return ResponseEntity.ok()
            .allow(OPTIONS, GET, PUT, POST, DELETE)
            .body(
                mapOf(
                    "OPTIONS /" to "returns all possible calls to the game",
                    "GET /" to "returns the current map with players information (plaintext)",
                    "POST /" to "create a new match (basic authentication: ADMIN only)",
                    "PUT /" to "enter the game (basic authentication: USER only)",
                    "PUT /?direction=x" to "move around the map (basic authentication: USER only) - accepted directions (1 char): n (north), s (south), e (east), w (west)",
                    "DELETE /?direction=x" to "delete something nearby the player on the map (basic authentication: USER only) - accepted directions (1 char): n (north), s (south), e (east), w (west)",
                )
            )
    }

    @GetMapping(produces = [TEXT_PLAIN_VALUE])
    fun getMatch(): String? {
        val levelMap = matchService.getLevelMap()
        val deletedPlayers = matchService.playerKilled.keys
        val onlinePlayers = matchService.playerPositions.keys - deletedPlayers
        return "$levelMap " +
                "\nOnline:  $onlinePlayers" +
                "\nDeleted: $deletedPlayers"
    }

    @PostMapping(produces = [APPLICATION_JSON_VALUE])
    fun createMatch(): ResponseEntity<Any> {
        matchService.generateLevel()
        return ResponseEntity(OK)
    }

    @PutMapping(produces = [APPLICATION_JSON_VALUE])
    fun playerIteration(
        authentication: Authentication,
        @RequestParam(required = false) direction: String?
    ): ResponseEntity<Any> {
        checkDirection(direction)

        val player = getPlayer(authentication)
        if (direction == null)
            matchService.enterMap(player)
        else
            matchService.move(player, direction.toLowerCase())
        return ResponseEntity(matchService.loadStatus(player), OK)
    }

    @DeleteMapping
    fun removeSomething(
        authentication: Authentication,
        @RequestParam direction: String
    ): ResponseEntity<Any> {
        checkDirection(direction)

        val player = getPlayer(authentication)
        matchService.delete(player, direction.toLowerCase())

        return if (matchService.isPlayerWinner(player))
            ResponseEntity("Congratulations! You won!!!!", OK)
        else
            ResponseEntity(matchService.loadStatus(player), OK)
    }

    private fun checkDirection(direction: String?) {
        if (direction?.toLowerCase() !in listOf(null, "n", "e", "s", "w"))
            throw ResponseStatusException(BAD_REQUEST, "Direction '$direction' is not valid")
    }

    private fun getPlayer(authentication: Authentication) =
        (authentication.principal as User).username.replace("player", "")

}
