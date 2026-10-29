package com.example.cinemap.data

import com.google.firebase.Timestamp

data class Notification {
    var id: String = "",
    var title: String = "",
    var message: String = "",
    var xp: Int = 0,
    var rewards: List<String> = emptyList(),
    var badgeId: String = "",
    var discount: Int = 0,
    var timestamp: Timestamp? = null,
    var isRead: Boolean = false
}