package com.example.purrsistence.domain.time

interface TimeProvider {
    fun now(): Long
}