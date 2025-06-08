package com.example.dp3akbpenjadwalan

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        // Ambil data dari intent
        val judul = intent.getStringExtra("judul")
        val deskripsi = intent.getStringExtra("deskripsi")
        val tanggal = intent.getStringExtra("tanggal")
        val waktu = intent.getStringExtra("waktu")
        val kategori = intent.getStringExtra("kategori")

        // Pasang data ke tampilan
        findViewById<TextView>(R.id.tvDetailJudul).text = judul
        findViewById<TextView>(R.id.tvDetailDeskripsi).text = deskripsi
        findViewById<TextView>(R.id.tvDetailTanggal).text = "Tanggal: $tanggal"
        findViewById<TextView>(R.id.tvDetailWaktu).text = "Waktu: $waktu"
        findViewById<TextView>(R.id.tvDetailKategori).text = "Kategori: $kategori"
    }
}
