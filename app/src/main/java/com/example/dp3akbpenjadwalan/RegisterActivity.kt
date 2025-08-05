package com.example.dp3akbpenjadwalan

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val editEmail = findViewById<TextInputEditText>(R.id.editEmailAddress)
        val editPassword = findViewById<TextInputEditText>(R.id.editPassword)
        val confirmPassword = findViewById<TextInputEditText>(R.id.editkonfirPassword)
        val buttonRegister = findViewById<Button>(R.id.buttonRegister)
        val textLogin = findViewById<TextView>(R.id.textLogin)
        val radioGroupRole = findViewById<RadioGroup>(R.id.radioGroupPilihan)

        textLogin.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        buttonRegister.setOnClickListener {
            val email = editEmail.text.toString().trim().lowercase()
            val password = editPassword.text.toString().trim()
            val confirmPass = confirmPassword.text.toString().trim()

            val selectedRoleId = radioGroupRole.checkedRadioButtonId
            if (selectedRoleId == -1) {
                Toast.makeText(this, "Pilih peran terlebih dahulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedRadioButton = findViewById<RadioButton>(selectedRoleId)
            val role = selectedRadioButton.text.toString()

            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                editEmail.error = "Email tidak valid"
                return@setOnClickListener
            }

            if (password.length < 6) {
                editPassword.error = "Password minimal 6 karakter"
                return@setOnClickListener
            }

            if (password != confirmPass) {
                confirmPassword.error = "Konfirmasi password tidak cocok"
                return@setOnClickListener
            }

            val adminRoles = listOf("Kabid PP", "Kabid PPA", "Kabid PUG", "Kabid KB", "Kepala UPTD")
            val isAdmin = role in adminRoles

            if (isAdmin) {
                // Periksa apakah email ada di whitelist admin
                firestore.collection("WhitelistAdmin")
                    .document(email)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            // Email ada di whitelist → lanjutkan registrasi
                            createAccount(email, password, role, isAdmin = true)
                        } else {
                            Toast.makeText(this, "Email tidak diizinkan menjadi admin", Toast.LENGTH_LONG).show()
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Gagal memeriksa whitelist: ${it.message}", Toast.LENGTH_LONG).show()
                    }
            } else {
                // Role bukan admin → langsung registrasi
                createAccount(email, password, role, isAdmin = false)
            }
        }
    }

    private fun createAccount(email: String, password: String, role: String, isAdmin: Boolean) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid ?: return@addOnCompleteListener

                    val userMap = hashMapOf(
                        "email" to email,
                        "role" to role,
                        "uid" to uid,
                        "isAdmin" to isAdmin
                    )

                    firestore.collection("users")
                        .document(uid)
                        .set(userMap)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Registrasi berhasil", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Gagal menyimpan data: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                } else {
                    Toast.makeText(this, "Registrasi gagal: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
}
