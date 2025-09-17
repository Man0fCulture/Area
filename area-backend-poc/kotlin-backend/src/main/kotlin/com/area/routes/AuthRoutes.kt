package com.area.routes

import at.favre.lib.crypto.bcrypt.BCrypt
import com.area.database.DatabaseManager
import com.area.models.*
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.mongodb.client.model.Filters
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.firstOrNull
import java.util.*

private val JWT_SECRET = System.getenv("JWT_SECRET") ?: "mysupersecretkeymysupersecretkeymysupersecretkey"
private val algorithm = Algorithm.HMAC256(JWT_SECRET)

fun Application.configureAuth() {
    routing {
        route("/api/auth") {
            post("/register") {
                val request = call.receive<UserRequest>()
                val databaseManager = DatabaseManager.getInstance()
                val database = databaseManager.getMongoDatabase("area")
                val usersCollection = database.getCollection<MongoUser>("users")

                // Check if user exists
                val existingUser = usersCollection.find(Filters.eq("email", request.email)).firstOrNull()

                if (existingUser != null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Email already registered"))
                    return@post
                }

                // Hash password
                val hashedPassword = BCrypt.withDefaults().hashToString(10, request.password.toCharArray())

                // Create user
                val newUser = MongoUser(
                    email = request.email,
                    password = hashedPassword,
                    firstName = request.firstName,
                    lastName = request.lastName
                )

                usersCollection.insertOne(newUser)

                // Generate token
                val token = JWT.create()
                    .withSubject(request.email)
                    .withClaim("userId", newUser.id)
                    .withExpiresAt(Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
                    .sign(algorithm)

                val response = AuthResponse(
                    token = token,
                    user = UserResponse(
                        id = newUser.id,
                        email = request.email,
                        firstName = request.firstName,
                        lastName = request.lastName
                    )
                )

                call.respond(HttpStatusCode.Created, response)
            }

            post("/login") {
                val request = call.receive<LoginRequest>()
                val databaseManager = DatabaseManager.getInstance()
                val database = databaseManager.getMongoDatabase("area")
                val usersCollection = database.getCollection<MongoUser>("users")

                // Find user
                val user = usersCollection.find(Filters.eq("email", request.email)).firstOrNull()

                if (user == null) {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid credentials"))
                    return@post
                }

                // Verify password
                val result = BCrypt.verifyer().verify(request.password.toCharArray(), user.password)
                if (!result.verified) {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid credentials"))
                    return@post
                }

                // Generate token
                val token = JWT.create()
                    .withSubject(user.email)
                    .withClaim("userId", user.id)
                    .withExpiresAt(Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
                    .sign(algorithm)

                val response = AuthResponse(
                    token = token,
                    user = UserResponse(
                        id = user.id,
                        email = user.email,
                        firstName = user.firstName,
                        lastName = user.lastName
                    )
                )

                call.respond(response)
            }
        }
    }
}