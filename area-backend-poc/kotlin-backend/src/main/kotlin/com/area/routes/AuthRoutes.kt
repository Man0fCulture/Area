package com.area.routes

import at.favre.lib.crypto.bcrypt.BCrypt
import com.area.models.*
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

private val JWT_SECRET = System.getenv("JWT_SECRET") ?: "mysupersecretkeymysupersecretkeymysupersecretkey"
private val algorithm = Algorithm.HMAC256(JWT_SECRET)

fun Application.configureAuth() {
    routing {
        route("/api/auth") {
            post("/register") {
                val request = call.receive<UserRequest>()

                // Check if user exists
                val existingUser = transaction {
                    Users.selectAll().where { Users.email eq request.email }.singleOrNull()
                }

                if (existingUser != null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Email already registered"))
                    return@post
                }

                // Hash password
                val hashedPassword = BCrypt.withDefaults().hashToString(10, request.password.toCharArray())

                // Create user
                val userId = transaction {
                    Users.insert {
                        it[email] = request.email
                        it[password] = hashedPassword
                        it[firstName] = request.firstName
                        it[lastName] = request.lastName
                    } get Users.id
                }

                // Generate token
                val token = JWT.create()
                    .withSubject(request.email)
                    .withClaim("userId", userId)
                    .withExpiresAt(Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
                    .sign(algorithm)

                val response = AuthResponse(
                    token = token,
                    user = UserResponse(
                        id = userId.toString(),
                        email = request.email,
                        firstName = request.firstName,
                        lastName = request.lastName
                    )
                )

                call.respond(HttpStatusCode.Created, response)
            }

            post("/login") {
                val request = call.receive<UserRequest>()

                // Find user
                val user = transaction {
                    Users.selectAll().where { Users.email eq request.email }.singleOrNull()
                }

                if (user == null) {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid credentials"))
                    return@post
                }

                // Verify password
                val result = BCrypt.verifyer().verify(request.password.toCharArray(), user[Users.password])
                if (!result.verified) {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid credentials"))
                    return@post
                }

                // Generate token
                val token = JWT.create()
                    .withSubject(user[Users.email])
                    .withClaim("userId", user[Users.id])
                    .withExpiresAt(Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
                    .sign(algorithm)

                val response = AuthResponse(
                    token = token,
                    user = UserResponse(
                        id = user[Users.id].toString(),
                        email = user[Users.email],
                        firstName = user[Users.firstName],
                        lastName = user[Users.lastName]
                    )
                )

                call.respond(response)
            }
        }
    }
}