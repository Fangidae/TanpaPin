package com.example.dp3akbpenjadwalan
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.os.Build
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    // Deklarasi Firebase Auth dan Firestore
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inisialisasi Firebase Auth dan Firestore
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        // Mengecek apakah pengguna sudah login sebelumnya (session disimpan di SharedPreferences)
        val sharedPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        val currentUser = auth.currentUser

        if (isLoggedIn && currentUser != null) {
            // User masih login, langsung ke dashboard
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
            return
        }

        // Jika belum login, tampilkan layout login
        setContentView(R.layout.activity_main)
        // Ambil referensi ke komponen UI
        val emailEditText = findViewById<EditText>(R.id.editTextEmailAddress)
        val passwordEditText = findViewById<EditText>(R.id.editTextPassword)
        val buttonLogin = findViewById<Button>(R.id.buttonLogin)
        val registerText = findViewById<TextView>(R.id.textRegister)
        val textForgetPassword = findViewById<TextView>(R.id.textForgetPassword)
        val radioGroupPilihan = findViewById<RadioGroup>(R.id.radioGroupPilihan)
        // Saat pengguna klik teks "Daftar", arahkan ke halaman registrasi
        registerText.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        textForgetPassword.setOnClickListener {
            startActivity(Intent(this, ResetpassActivity::class.java))
        }
        // Saat tombol Login ditekan
        buttonLogin.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            // Validasi email tidak kosong dan format email valid
            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailEditText.error = "Email tidak valid"
                return@setOnClickListener
            }
            // Validasi password tidak kosong
            if (password.isEmpty()) {
                passwordEditText.error = "Kata sandi tidak boleh kosong"
                return@setOnClickListener
            }
            // Validasi bahwa user memilih peran/admin (melalui radio button)
            val selectedRoleId = radioGroupPilihan.checkedRadioButtonId
            if (selectedRoleId == -1) {
                Toast.makeText(this, "Pilih salah satu peran", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Ambil nilai peran yang dipilih (Admin )
            val selectedRole = findViewById<RadioButton>(selectedRoleId).text.toString()
            // Lakukan login menggunakan Firebase Auth
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
                        // Ambil data user dari Firestore
                        firestore.collection("users").document(userId)
                            .get()
                            .addOnSuccessListener { document ->
                                val storedRole = document.getString("role")
                                // Cek apakah role di Firestore sesuai dengan role yang dipilih
                                if (storedRole != null && storedRole == selectedRole) {

                                    // Simpan status login di SharedPreferences agar tidak logout otomatis
                                    sharedPreferences.edit().putBoolean("isLoggedIn", true).apply()

                                    Toast.makeText(this, "Login berhasil sebagai $storedRole", Toast.LENGTH_SHORT).show()
                                    // Arahkan ke DashboardActivity dan kirim data role
                                    val intent = Intent(this, DashboardActivity::class.java)
                                    intent.putExtra("ROLE", storedRole)
                                    startActivity(intent)
                                    finish()
                                } else {
                                    // Gagal mengambil data dari Firestore
                                    auth.signOut()
                                    Toast.makeText(this, "Role tidak cocok dengan akun ini!", Toast.LENGTH_LONG).show()
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
    // Fungsi untuk menangani hasil permintaan izin notifikasi (khusus Android 13+)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Izin notifikasi diberikan", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Izin notifikasi ditolak", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
