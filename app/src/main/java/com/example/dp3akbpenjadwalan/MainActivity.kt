package com.example.dp3akbpenjadwalan

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val emailEditText = findViewById<EditText>(R.id.editTextEmailAddress)
        val passwordEditText = findViewById<EditText>(R.id.editTextPassword)
        val buttonLogin = findViewById<Button>(R.id.buttonLogin)
        val registerText = findViewById<TextView>(R.id.textRegister)
        val textForgetPassword = findViewById<TextView>(R.id.textForgetPassword)
        val radioGroupPilihan = findViewById<RadioGroup>(R.id.radioGroupPilihan)

        registerText.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        textForgetPassword.setOnClickListener {
            startActivity(Intent(this, ResetpassActivity::class.java))
        }

        buttonLogin.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailEditText.error = "Email tidak valid"
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                passwordEditText.error = "Kata sandi tidak boleh kosong"
                return@setOnClickListener
            }

            val selectedRoleId = radioGroupPilihan.checkedRadioButtonId
            if (selectedRoleId == -1) {
                Toast.makeText(this, "Pilih salah satu peran", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedRole = findViewById<RadioButton>(selectedRoleId).text.toString()

            // Login dengan FirebaseAuth
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid ?: return@addOnCompleteListener

                        // Ambil data user dari Firestore
                        firestore.collection("users").document(userId)
                            .get()
                            .addOnSuccessListener { document ->
                                if (document != null && document.exists()) {
                                    val storedRole = document.getString("role")
                                    if (storedRole == selectedRole) {
                                        // Role cocok, lanjut ke Dashboard
                                        Toast.makeText(this, "Login berhasil sebagai $storedRole", Toast.LENGTH_SHORT).show()
                                        val intent = Intent(this, DashboardActivity::class.java)
                                        intent.putExtra("ROLE", storedRole)
                                        startActivity(intent)
                                        finish()
                                    } else {
                                        // Role tidak sesuai, logout
                                        auth.signOut()
                                        Toast.makeText(this, "Role tidak cocok dengan akun ini!", Toast.LENGTH_LONG).show()
                                    }
                                } else {
                                    auth.signOut()
                                    Toast.makeText(this, "Data pengguna tidak ditemukan", Toast.LENGTH_LONG).show()
                                }
                            }
                            .addOnFailureListener {
                                auth.signOut()
                                Toast.makeText(this, "Gagal memuat data pengguna", Toast.LENGTH_LONG).show()
                            }
                    } else {
                        Toast.makeText(this, "Login gagal: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}
