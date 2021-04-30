package de.openvalue.restkiller.service

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest
class MatchServiceTest {

    @Autowired
    lateinit var matchService: MatchService

    @Test
    fun `generate a new level`() {
        matchService.generateLevel()
        matchService.level.forEach { line ->
            line.forEach { print(it) }; print("\n")
        }
    }

}
