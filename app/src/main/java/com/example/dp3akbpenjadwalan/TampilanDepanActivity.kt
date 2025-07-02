package com.example.dp3akbpenjadwalan

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class TampilanDepanActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tampilan_depan)

        val btnAdmin = findViewById<Button>(R.id.btn_admin)
        val btnPegawai = findViewById<Button>(R.id.btn_pegawai)

        btnAdmin.setOnClickListener {
            showPinDialog() // Ganti intent langsung → dengan validasi PIN
        }

        btnPegawai.setOnClickListener {
            val intent = Intent(this, DashboardPegawaiActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showPinDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_input_pin, null)
        val inputPin = dialogView.findViewById<EditText>(R.id.editTextPin)

        AlertDialog.Builder(this)
            .setTitle("Masukkan PIN Admin")
            .setView(dialogView)
            .setPositiveButton("Lanjutkan") { _, _ ->
                val pin = inputPin.text.toString()
                verifyPin(pin)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun verifyPin(inputPin: String) {
        val db = FirebaseFirestore.getInstance()
        val pinRef = db.collection("admin_config").document("pin")

        pinRef.get().addOnSuccessListener { doc ->
            val savedPin = doc.getString("value")
            if (inputPin == savedPin) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "PIN salah", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Gagal menghubungi server", Toast.LENGTH_SHORT).show()
        }
    }
}
