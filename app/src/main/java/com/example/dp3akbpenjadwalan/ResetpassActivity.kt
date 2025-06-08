package com.example.dp3akbpenjadwalan

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class ResetpassActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var emailInput: EditText
    private lateinit var btnSendReset: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resetpass)

        // Inisialisasi Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Referensi view
        emailInput = findViewById(R.id.emailInput)
        btnSendReset = findViewById(R.id.btnSendReset)

        btnSendReset.setOnClickListener {
            val email = emailInput.text.toString().trim()

            if (email.isEmpty()) {
                emailInput.error = "Email tidak boleh kosong"
                return@setOnClickListener
            }

            // Kirim email reset password lewat Firebase
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Email reset berhasil dikirim", Toast.LENGTH_LONG).show()
                        finish() // Kembali ke login
                    } else {
                        Toast.makeText(this, "Gagal mengirim email: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}
