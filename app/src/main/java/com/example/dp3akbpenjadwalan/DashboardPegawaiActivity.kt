package com.example.dp3akbpenjadwalan

import KegiatanModel
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class DashboardPegawaiActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: KegiatanAdapter
    private lateinit var kegiatanList: ArrayList<KegiatanModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_pegawai)

        recyclerView = findViewById(R.id.recyclerViewPegawai)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val db = FirebaseFirestore.getInstance()
        kegiatanList = arrayListOf()

        db.collection("kegiatan")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val kegiatan = document.toObject(KegiatanModel::class.java)
                    kegiatan.id = document.id
                    kegiatanList.add(kegiatan)
                }

                adapter = KegiatanAdapter(
                    context = this,
                    list = kegiatanList,
                    onItemClick = { kegiatan -> /* detail, jika perlu */ },
                    onEditClick = null,
                    onDeleteClick = null,
                    role = "pegawai" // atau "user", yang penting bukan "admin"
                )

                recyclerView.adapter = adapter
            }
            .addOnFailureListener {
                // Handle error
            }


        adapter = KegiatanAdapter(
            context = this,
            list = kegiatanList,
            onItemClick = { kegiatan ->
                // misalnya tampil detail
            },
            onEditClick = null,
            onDeleteClick = null,
            role = "pegawai" // ✅ tambahkan ini
        )

        recyclerView.adapter = adapter
    }
}
