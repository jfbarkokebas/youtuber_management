package br.com.felipe.creator.infrastructure.persistence

import java.sql.Connection
import java.sql.DriverManager

object DatabaseFactory {

    private val url = System.getenv("DB_URL")
        ?: "jdbc:mysql://localhost:3306/kotlin_tasks"

    private val user = System.getenv("DB_USER")
        ?: error("Variável DB_USER não configurada")

    private val password = System.getenv("DB_PASSWORD")
        ?: error("Variável DB_PASSWORD não configurada")

    fun getConnection(): Connection {
        return DriverManager.getConnection(url, user, password)
    }

    fun testConnection() {

        getConnection().use { connection ->

            connection.prepareStatement("SELECT 1").use { statement ->

                statement.executeQuery().use { result ->

                    if (result.next()) {
                        println("Conexão com MySQL estabelecida!")
                        println("Resultado do teste: ${result.getInt(1)}")
                    }

                }
            }
        }
    }
}