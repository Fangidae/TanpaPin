package com.example.dp3akbpenjadwalan

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PengaturanAdminActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pengaturan_admin)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        firestore = FirebaseFirestore.getInstance()

        // Ambil nama pengguna dari SharedPreferences
        val emailTextView = findViewById<TextView>(R.id.EMAIL)
        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val namaPengguna = sharedPref.getString("namaPengguna", "") ?: ""

        // Tampilkan nama + email langsung
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

        // Tombol Ubah PIN Admin
        val ubahPin = findViewById<TextView>(R.id.ubahPin)
        ubahPin.setOnClickListener {
            showUbahPinDialog()
        }
    }

    private fun showLogoutDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Konfirmasi Logout")
        builder.setMessage("Apakah Anda yakin ingin keluar?")
        builder.setPositiveButton("Ya") { _, _ -> logoutUser() }
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

    private fun showUbahPinDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_ubah_pin, null)
        val inputPinLama = dialogView.findViewById<EditText>(R.id.pinLama)
        val inputPinBaru = dialogView.findViewById<EditText>(R.id.pinBaru)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Ubah PIN Admin")
            .setView(dialogView)
            .setPositiveButton("Simpan", null)
            .setNegativeButton("Batal", null)
            .create()

        dialog.setOnShowListener {
            val buttonSimpan = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            buttonSimpan.setOnClickListener {
                val pinLama = inputPinLama.text.toString().trim()
                val pinBaru = inputPinBaru.text.toString().trim()

                if (pinLama.isEmpty() || pinBaru.isEmpty()) {
                    showMessage("Semua kolom harus diisi.")
                    return@setOnClickListener
                }

                if (pinBaru.length < 4) {
                    showMessage("PIN baru harus minimal 4 digit.")
                    return@setOnClickListener
                }

                val pinDocRef = firestore.collection("admin_config").document("pin")
                pinDocRef.get()
                    .addOnSuccessListener { document ->
                        val currentPin = document.getString("value")
                        if (currentPin == pinLama) {
                            pinDocRef.update("value", pinBaru)
                                .addOnSuccessListener {
                                    showMessage("PIN berhasil diperbarui.")
                                    dialog.dismiss()
                                }
                                .addOnFailureListener {
                                    showMessage("Gagal memperbarui PIN.")
                                }
                        } else {
                            showMessage("PIN lama salah.")
                        }
                    }
                    .addOnFailureListener {
                        showMessage("Terjadi kesalahan saat mengambil data PIN.")
                    }
            }
        }

        dialog.show()
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
