package br.com.felipe.creator

import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {

        embeddedServer(
                Netty,
                port = 8080,
                host = "0.0.0.0",
                module = {
                        configureApplication()
                }

        ).start(wait = true)
}
