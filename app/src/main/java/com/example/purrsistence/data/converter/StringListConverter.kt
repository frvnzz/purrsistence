package com.example.purrsistence.data.converter

import androidx.room.TypeConverter
import kotlinx.serialization.json.Json

class StringListConverter {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return json.encodeToString(value ?: emptyList())
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        return json.decodeFromString(value)
    }
}