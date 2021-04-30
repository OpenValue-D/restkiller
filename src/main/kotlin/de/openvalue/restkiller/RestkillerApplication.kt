package de.openvalue.restkiller

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class RestkillerApplication

fun main(args: Array<String>) {
    runApplication<RestkillerApplication>(*args)
}
