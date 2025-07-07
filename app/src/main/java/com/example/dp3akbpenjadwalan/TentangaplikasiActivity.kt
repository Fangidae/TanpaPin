package com.example.dp3akbpenjadwalan

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.dp3akbpenjadwalan.databinding.ActivityTentangaplikasiBinding

class TentangaplikasiActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTentangaplikasiBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTentangaplikasiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Tentang Aplikasi"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.textTentangAplikasi.text = """
            📱 Aplikasi SEPAKAT
            -----------------------
            Sistem Penjadwalan Kegiatan Terintegrasi DP3AKB Provinsi Papua
            
            Versi: 1.0.0
            
            SEPAKAT merupakan aplikasi yang dikembangkan untuk membantu pengelolaan jadwal kegiatan secara digital dan terintegrasi, khususnya di lingkungan Dinas Pemberdayaan Perempuan, Perlindungan Anak dan Keluarga Berencana (DP3AKB) Provinsi Papua.
            
            Fitur Utama:
            - Penjadwalan kegiatan dinas
            - Manajemen akun pengguna
            
            Dikembangkan oleh: Grace Pananggung, Juan Fangidae, Rahmat Hidayah
            Email pengembang: support@sepakat.com
        """.trimIndent()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
