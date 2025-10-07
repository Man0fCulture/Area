package com.epitech.area.domain.repositories

import com.epitech.area.domain.entities.Service
import org.bson.types.ObjectId

interface ServiceRepository {
    suspend fun create(service: Service): Service
    suspend fun findById(id: ObjectId): Service?
    suspend fun findByName(name: String): Service?
    suspend fun findAll(): List<Service>
    suspend fun findAllEnabled(): List<Service>
    suspend fun update(service: Service): Service
    suspend fun delete(id: ObjectId): Boolean
}
