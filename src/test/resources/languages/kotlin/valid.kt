package com.example

data class User(val id: Int, val name: String)

interface Repository {
    fun addUser(user: User)
    fun findUser(id: Int): User?
}

fun main() {
    val repo = null
    println("Hello Kotlin")
}
