package com.example.purrsistence.domain.model

data class User(
    val id: Int,
    val username: String,
    val balance: Int,
    val friends: List<String>,
    val collectedCatsIds: List<String>,
    val selectedCatIds: List<String>
)