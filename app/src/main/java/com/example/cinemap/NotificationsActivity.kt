package com.example.cinemap

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.cinemap.data.Notification
import com.example.cinemap.databinding.ActivityNotificationsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

class NotificationsActivity : BaseActivity() {

    private lateinit var binding: ActivityNotificationsBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var allNotifications = listOf<Notification>()
    private var currentFilter = "All"

    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = getSharedPreferences("theme", MODE_PRIVATE)
        val isDark = prefs.getBoolean("isDark", true)
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadNotifications()

        binding.chipAll.setOnClickListener { filter("All") }
        binding.chipUnread.setOnClickListener { filter("Unread") }
        binding.chipRead.setOnClickListener { filter("Read") }
        binding.chipTickets.setOnClickListener { filter("Tickets") }
        binding.chipRewards.setOnClickListener { filter("Rewards") }

        binding.btnMarkAll.setOnClickListener {
            markAllRead()
        }

        binding.btnHome.setOnClickListener { finish() }

        // bottom nav
        findViewById<LinearLayout>(R.id.navHome).setOnClickListener { startActivity(Intent(this, MainActivity::class.java)) }
        findViewById<LinearLayout>(R.id.navRewards).setOnClickListener { startActivity(Intent(this, RewardsActivity::class.java)) }
        findViewById<LinearLayout>(R.id.navNotifications).setOnClickListener { }
        findViewById<LinearLayout>(R.id.navSettings).setOnClickListener { startActivity(Intent(this, SettingsActivity::class.java)) }
    }

    private fun loadNotifications() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).collection("notifications")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ ->
                if (snap == null) return@addSnapshotListener
                allNotifications = snap.documents.mapNotNull {
                    val n = it.toObject(Notification::class.java)
                    n?.copy(id = it.id)
                }
                updateUnread()
                filter(currentFilter)
            }
    }

    private fun filter(type: String) {
        currentFilter = type
        // update chip UI
        binding.chipAll.setBackgroundResource(if (type == "All") R.drawable.bg_chip_selected else R.drawable.bg_chip_unselected)
        binding.chipUnread.setBackgroundResource(if (type == "Unread") R.drawable.bg_chip_selected else R.drawable.bg_chip_unselected)
        binding.chipRead.setBackgroundResource(if (type == "Read") R.drawable.bg_chip_selected else R.drawable.bg_chip_unselected)
        binding.chipTickets.setBackgroundResource(if (type == "Tickets") R.drawable.bg_chip_selected else R.drawable.bg_chip_unselected)
        binding.chipRewards.setBackgroundResource(if (type == "Rewards") R.drawable.bg_chip_selected else R.drawable.bg_chip_unselected)

        val selectedTextColor = 0xFF0B1020.toInt()
        val unselectedTextColor = getColor(R.color.text_secondary)

        binding.chipAll.setTextColor(if (type == "All") selectedTextColor else unselectedTextColor)
        binding.chipUnread.setTextColor(if (type == "Unread") selectedTextColor else unselectedTextColor)
        binding.chipRead.setTextColor(if (type == "Read") selectedTextColor else unselectedTextColor)
        binding.chipTickets.setTextColor(if (type == "Tickets") selectedTextColor else unselectedTextColor)
        binding.chipRewards.setTextColor(if (type == "Rewards") selectedTextColor else unselectedTextColor)

        val filtered = when (type) {
            "Unread" -> allNotifications.filter { !it.isRead }
            "Read" -> allNotifications.filter { it.isRead }
            "Tickets" -> allNotifications.filter { it.title.contains("Booking") || it.title.contains("Movie") || it.badgeId.contains("ticket") }
            "Rewards" -> allNotifications.filter { it.title.contains("XP") || it.title.contains("Reward") || it.xp > 0 }
            else -> allNotifications
        }
        renderList(filtered)
    }

    private fun renderList(list: List<Notification>) {
        binding.notificationsContainer.removeAllViews()
        if (list.isEmpty()) {
            val empty = TextView(this).apply {
                text = "You're all caught up."
                setTextColor(getColor(R.color.text_secondary))
                setPadding(0, 100, 0, 0)
                gravity = Gravity.CENTER
            }
            binding.notificationsContainer.addView(empty)
            return
        }

        // Group by TODAY / YESTERDAY
        val today = mutableListOf<Notification>()
        val yesterday = mutableListOf<Notification>()
        val older = mutableListOf<Notification>()

        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val todayStr = sdf.format(Date())
        val yesterdayStr = sdf.format(Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }.time)

        for (n in list) {
            val dateStr = n.timestamp?.toDate()?.let { sdf.format(it) } ?: todayStr
            when (dateStr) {
                todayStr -> today.add(n)
                yesterdayStr -> yesterday.add(n)
                else -> older.add(n)
            }
        }

        if (today.isNotEmpty()) addSection("TODAY", today)
        if (yesterday.isNotEmpty()) addSection("YESTERDAY", yesterday)
        if (older.isNotEmpty()) addSection("OLDER", older)
    }

    private fun addSection(header: String, items: List<Notification>) {
        val headerView = TextView(this).apply {
            text = header
            textSize = 10f
            setTextColor(getColor(R.color.text_secondary))
            setPadding(0, 16, 0, 8)
        }
        binding.notificationsContainer.addView(headerView)

        for (item in items) {
            val isTicket = item.title.contains("Booking", true) || item.title.contains("Ticket", true)
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setBackgroundResource(R.drawable.bg_card)
                setPadding(32, 32, 32, 32)
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                    bottomMargin = 24
                }
                setOnClickListener {
                    // Mark as read when clicked
                    if (!item.isRead) {
                        val uid = auth.currentUser?.uid ?: return@setOnClickListener
                        db.collection("users").document(uid).collection("notifications").document(item.id).update("isRead", true)
                    }
                }
            }

            val topRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            topRow.addView(TextView(this).apply {
                text = if (isTicket) "🎟 BOOKING CONFIRMED" else "⭐ REWARDS UPDATE"
                textSize = 10f
                setTextColor(if (isTicket) 0xFF6C5CE7.toInt() else 0xFF00B894.toInt())
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            })
            topRow.addView(TextView(this).apply {
                text = formatTime(item.timestamp?.toDate())
                textSize = 10f
                setTextColor(getColor(R.color.text_secondary))
            })

            val title = TextView(this).apply {
                text = item.title
                textSize = 14f
                setTextColor(getColor(R.color.text_primary))
                setTypeface(null, Typeface.BOLD)
                setPadding(0, 12, 0, 0)
            }
            val msg = TextView(this).apply {
                text = item.message
                textSize = 12f
                setTextColor(getColor(R.color.text_secondary))
            }

            card.addView(topRow)
            card.addView(title)
            card.addView(msg)
            binding.notificationsContainer.addView(card)
        }
    }

    private fun formatTime(date: Date?): String {
        if (date == null) return "Just now"
        val diff = (Date().time - date.time) / 1000 / 60
        return when {
            diff < 1 -> "Just now"
            diff < 60 -> "${diff}m ago"
            diff < 1440 -> "${diff/60} hrs ago"
            else -> SimpleDateFormat("Yesterday", Locale.getDefault()).format(date)
        }
    }

    private fun updateUnread() {
        val unread = allNotifications.count { !it.isRead }
        binding.tvUnread.text = "You have $unread unread alerts."
    }

    private fun markAllRead() {
        val uid = auth.currentUser?.uid ?: return
        allNotifications.forEach { n ->
            db.collection("users").document(uid).collection("notifications").document(n.id).update("isRead", true)
        }
        Toast.makeText(this, "Marked all as read", Toast.LENGTH_SHORT).show()
    }
}
