package com.epitech.area.infrastructure.plugins

import com.epitech.area.infrastructure.serialization.ObjectIdSerializer
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import org.bson.types.ObjectId

fun Application.configureSerialization() {
    val module = SerializersModule {
        contextual(ObjectId::class, ObjectIdSerializer)
    }

    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
            serializersModule = module
        })
    }
}
