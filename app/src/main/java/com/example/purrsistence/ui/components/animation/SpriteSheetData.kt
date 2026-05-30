package com.example.purrsistence.ui.components.animation

data class SpriteSheetData(
    val columns: Int,
    val rows: Int,
    val totalFrames: Int,
    val frameDurationMs: Long = 100L
)
