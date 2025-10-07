package com.epitech.area.infrastructure.security

import de.mkammerer.argon2.Argon2Factory

object PasswordEncoder {
    private val argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id)

    fun encode(password: String): String {
        return argon2.hash(10, 65536, 1, password.toCharArray())
    }

    fun verify(password: String, hash: String): Boolean {
        return try {
            argon2.verify(hash, password.toCharArray())
        } catch (e: Exception) {
            false
        }
    }
}
