package com.example.dp3akbpenjadwalan

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        //  Ambil data dari intent yang dikirim dari activity sebelumnya
        val judul = intent.getStringExtra("judul")
        val deskripsi = intent.getStringExtra("deskripsi")
        val tempat = intent.getStringExtra("tempat")
        val tanggal = intent.getStringExtra("tanggal")
        val waktu = intent.getStringExtra("waktu")
        val kategori = intent.getStringExtra("kategori")
        val bidang = intent.getStringExtra("bidang")

        // Tampilkan data ke komponen TextView di layout
        findViewById<TextView>(R.id.tvDetailJudul).text = "Topik Kegiatan: $judul"
        findViewById<TextView>(R.id.tvDetailDeskripsi).text = "Peserta: $deskripsi"
        findViewById<TextView>(R.id.tvDetailTempat).text = "Tempat: $tempat"
        findViewById<TextView>(R.id.tvDetailTanggal).text = "Tanggal: $tanggal"
        findViewById<TextView>(R.id.tvDetailWaktu).text = "Waktu: $waktu"
        findViewById<TextView>(R.id.tvDetailKategori).text = "Kategori: $kategori"
        findViewById<TextView>(R.id.tvDetailBidang).text = "Bidang: $bidang"
    }
}
