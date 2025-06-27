package com.example.dp3akbpenjadwalan

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class PengaturanAdminActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pengaturan_admin)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Ambil nama pengguna dari SharedPreferences
        val emailTextView = findViewById<TextView>(R.id.EMAIL)
        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val namaPengguna = sharedPref.getString("namaPengguna", "") ?: ""

        // Tampilkan nama + email langsung tanpa kata "Pengguna"
        val email = FirebaseAuth.getInstance().currentUser?.email ?: "Email tidak tersedia"
        emailTextView.text = "$namaPengguna\n$email"


        // Tombol Tentang Aplikasi
        val tentang = findViewById<TextView>(R.id.tentang)
        tentang.setOnClickListener {
            val intent = Intent(this, TentangaplikasiActivity::class.java)
            startActivity(intent)
        }

        // Tombol Logout
        val logout = findViewById<TextView>(R.id.LOGOUT)
        logout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            val preferences = getSharedPreferences("loginPrefs", MODE_PRIVATE)
            preferences.edit().clear().apply()

            startActivity(Intent(this, TampilanDepanActivity::class.java))
            finish()
        }
    }

    private fun showLogoutDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Konfirmasi Logout")
        builder.setMessage("Apakah Anda yakin ingin keluar?")
        builder.setPositiveButton("Ya") { _, _ ->
            logoutUser()
        }
        builder.setNegativeButton("Batal", null)
        builder.show()
    }

    private fun logoutUser() {
        FirebaseAuth.getInstance().signOut()

        val preferences = getSharedPreferences("loginPrefs", MODE_PRIVATE)
        preferences.edit().putBoolean("isLoggedIn", false).apply()

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
