package com.example.dp3akbpenjadwalan

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class TampilanDepanActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tampilan_depan)

        val btnAdmin = findViewById<Button>(R.id.btn_admin)
        val btnPegawai = findViewById<Button>(R.id.btn_pegawai)

        btnAdmin.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        btnPegawai.setOnClickListener {
            val intent = Intent(this, DashboardPegawaiActivity::class.java)
            startActivity(intent)
        }
    }
}
