package com.example.dp3akbpenjadwalan

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Patterns
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Inisialisasi Firebase Auth
        auth = FirebaseAuth.getInstance()

        val editEmail = findViewById<EditText>(R.id.editTextemail2)
        val editPassword = findViewById<EditText>(R.id.editpassword2)
        val confirmPassword = findViewById<EditText>(R.id.editConfirmPassword2)
        val showPasswordCheckbox = findViewById<CheckBox>(R.id.cb_show_password2)
        val buttonRegister = findViewById<Button>(R.id.buttonRegister)
        val textLogin = findViewById<TextView>(R.id.textLogin)

        textLogin.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        showPasswordCheckbox.setOnCheckedChangeListener { _, isChecked ->
            val inputTypeVisible = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            val inputTypeHidden = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

            editPassword.inputType = if (isChecked) inputTypeVisible else inputTypeHidden
            confirmPassword.inputType = if (isChecked) inputTypeVisible else inputTypeHidden

            editPassword.setSelection(editPassword.text.length)
            confirmPassword.setSelection(confirmPassword.text.length)
        }

        buttonRegister.setOnClickListener {
            val email = editEmail.text.toString().trim()
            val password = editPassword.text.toString().trim()
            val confirmPass = confirmPassword.text.toString().trim()

            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                editEmail.error = "Email tidak valid"
                return@setOnClickListener
            }

            if (password.length < 6) {
                editPassword.error = "Password minimal 6 karakter"
                return@setOnClickListener
            }

            if (password != confirmPass) {
                confirmPassword.error = "Konfirmasi password tidak sama"
                return@setOnClickListener
            }

            // Buat user dengan Firebase
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Registrasi berhasil", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, DashboardActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Registrasi gagal: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}
