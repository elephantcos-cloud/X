package com.example.proqrscanner.domain.model

data class ScanItem(
    val id: Long,
    val rawValue: String,
    val type: String,
    val timestamp: String
)
