package br.com.felipe.creator.presentation.http.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.http.*

fun Application.configureCors() {

    install(CORS) {
        allowHost(
            "127.0.0.1:5500",
            schemes = listOf("http")
        )

        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)

        allowHeader(HttpHeaders.ContentType)

        // Cabeçalhos enviados pelo HTMX
        allowHeader("HX-Current-URL")
        allowHeader("HX-Request")
        allowHeader("HX-Target")
        allowHeader("HX-Trigger")
    }
}