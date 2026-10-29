package com.example.cinemap.data

import com.google.firebase.Timestamp

data class Notification(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val xp: Int = 0,
    val rewards: List<String> = emptyList(),
    val badgeId: String = "",
    val discount: Int = 0,
    val timestamp: Timestamp? = null,
    val isRead: Boolean = false
)
