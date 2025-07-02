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

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: KegiatanAdapter
    private lateinit var kegiatanList: ArrayList<KegiatanModel>
    private lateinit var allKegiatanList: ArrayList<KegiatanModel>

    private lateinit var searchView: SearchView
    private var selectedBidangFilter: String? = null
    private var currentSearchQuery: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_pegawai)

        recyclerView = findViewById(R.id.recyclerViewPegawai)
        recyclerView.layoutManager = LinearLayoutManager(this)

        searchView = findViewById(R.id.searchView)
        kegiatanList = arrayListOf()
        allKegiatanList = arrayListOf()

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
            onEditClick = null,
            onDeleteClick = null,
            role = "pegawai"
        )

        recyclerView.adapter = adapter

        setupBidangButtons()
        setupSearchView()
        loadData()
    }

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

    private fun loadData() {
        val db = FirebaseFirestore.getInstance()
        db.collection("kegiatan")
            .get()
            .addOnSuccessListener { result ->
                kegiatanList.clear()
                allKegiatanList.clear()

                for (document in result) {
                    val kegiatan = document.toObject(KegiatanModel::class.java)
                    kegiatan.id = document.id
                    allKegiatanList.add(kegiatan)
                }

                filterKegiatan()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal memuat data", Toast.LENGTH_SHORT).show()
            }
    }

    private fun filterKegiatan() {
        kegiatanList.clear()
        for (kegiatan in allKegiatanList) {
            val matchBidang = selectedBidangFilter == null || kegiatan.bidang?.equals(selectedBidangFilter, ignoreCase = true) == true
            val matchQuery = currentSearchQuery.isNullOrBlank() ||
                    kegiatan.judul.orEmpty().contains(currentSearchQuery!!, ignoreCase = true) ||
                    kegiatan.deskripsi.orEmpty().contains(currentSearchQuery!!, ignoreCase = true)

            if (matchBidang && matchQuery) {
                kegiatanList.add(kegiatan)
            }
        }

        adapter.notifyDataSetChanged()
    }
}
