package com.example.dp3akbpenjadwalan

import KegiatanModel
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import androidx.appcompat.widget.SearchView

class DashboardPegawaiActivity : AppCompatActivity() {
    // Deklarasi komponen UI dan variabel data
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: KegiatanAdapter
    private lateinit var kegiatanList: ArrayList<KegiatanModel> // List yang ditampilkan di RecyclerView
    private lateinit var allKegiatanList: ArrayList<KegiatanModel> // List semua data dari Firestore

    private lateinit var searchView: SearchView     // SearchView untuk pencarian
    private var selectedBidangFilter: String? = null    // Filter berdasarkan bidang
    private var currentSearchQuery: String? = null       // Filter berdasarkan keyword pencarian

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_pegawai)
        // Inisialisasi RecyclerView
        recyclerView = findViewById(R.id.recyclerViewPegawai)
        recyclerView.layoutManager = LinearLayoutManager(this)
        // Inisialisasi komponen dan list
        searchView = findViewById(R.id.searchView)
        kegiatanList = arrayListOf()
        allKegiatanList = arrayListOf()

        // Inisialisasi adapter dengan item click ke DetailActivity, tanpa fitur edit & hapus
        adapter = KegiatanAdapter(
            context = this,
            list = kegiatanList,
            onItemClick = { kegiatan ->
                val intent = Intent(this, DetailActivity::class.java)
                intent.putExtra("judul", kegiatan.judul)
                intent.putExtra("deskripsi", kegiatan.deskripsi)
                intent.putExtra("tempat", kegiatan.tempat)
                intent.putExtra("tanggal", kegiatan.tanggal)
                intent.putExtra("waktu", kegiatan.waktu)
                intent.putExtra("kategori", kegiatan.kategori)
                intent.putExtra("bidang", kegiatan.bidang)
                startActivity(intent)
            },
            onEditClick = null, // Tidak ada fitur edit untuk pegawai
            onDeleteClick = null,   // Tidak ada fitur hapus untuk pegawai
            role = "pegawai"    // Role pegawai dikirim ke adapter
        )

        recyclerView.adapter = adapter   // Pasang adapter ke RecyclerView

        setupBidangButtons()    // Atur tombol filter bidang
        setupSearchView()       // Atur fungsi pencarian
        loadData()              // Ambil data kegiatan dari Firestore
    }

    // Fungsi untuk mengatur tombol filter berdasarkan bidang
    private fun setupBidangButtons() {
        findViewById<Button>(R.id.btnAllBidang).setOnClickListener {
            selectedBidangFilter = null
            filterKegiatan()
        }

        findViewById<Button>(R.id.btnUPTD).setOnClickListener {
            selectedBidangFilter = "UPTD PPA"
            filterKegiatan()
        }

        findViewById<Button>(R.id.btnPPA).setOnClickListener {
            selectedBidangFilter = "PPA"
            filterKegiatan()
        }

        findViewById<Button>(R.id.btnPP).setOnClickListener {
            selectedBidangFilter = "PP"
            filterKegiatan()
        }

        findViewById<Button>(R.id.btnPUG).setOnClickListener {
            selectedBidangFilter = "PUG"
            filterKegiatan()
        }

        findViewById<Button>(R.id.btnKB).setOnClickListener {
            selectedBidangFilter = "KB"
            filterKegiatan()
        }
    }

    // Fungsi untuk mengatur pencarian dari SearchView
    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                currentSearchQuery = query
                filterKegiatan()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                currentSearchQuery = newText
                filterKegiatan()
                return true
            }
        })
    }
    // Fungsi untuk mengambil data kegiatan dari Firestore
    private fun loadData() {
        val db = FirebaseFirestore.getInstance()
        db.collection("kegiatan")   // Ambil semua data dari koleksi global "kegiatan"
            .get()
            .addOnSuccessListener { result ->
                kegiatanList.clear()
                allKegiatanList.clear()

                for (document in result) {
                    val kegiatan = document.toObject(KegiatanModel::class.java)
                    kegiatan.id = document.id   // Simpan ID dokumen
                    allKegiatanList.add(kegiatan)   // Simpan semua data ke list lengkap
                }

                filterKegiatan()    // Tampilkan data setelah di-load
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal memuat data", Toast.LENGTH_SHORT).show()
            }
    }
    // Fungsi untuk memfilter data berdasarkan bidang dan pencarian
    private fun filterKegiatan() {
        kegiatanList.clear()
        for (kegiatan in allKegiatanList) {
            // Cek apakah bidang cocok dengan filter
            val matchBidang = selectedBidangFilter == null || kegiatan.bidang?.equals(selectedBidangFilter, ignoreCase = true) == true
            val matchQuery = currentSearchQuery.isNullOrBlank() ||
                    kegiatan.judul.orEmpty().contains(currentSearchQuery!!, ignoreCase = true) ||
                    kegiatan.deskripsi.orEmpty().contains(currentSearchQuery!!, ignoreCase = true)

            if (matchBidang && matchQuery) {
                kegiatanList.add(kegiatan)
            }
        }

        adapter.notifyDataSetChanged()  // Cek apakah bidang cocok dengan filter
    }
}
