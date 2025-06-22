package com.example.dp3akbpenjadwalan

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dp3akbpenjadwalan.model.KegiatanModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class DashboardActivity : AppCompatActivity() {

    private lateinit var kegiatanList: ArrayList<KegiatanModel>
    private lateinit var allKegiatanList: ArrayList<KegiatanModel>
    private lateinit var adapter: KegiatanAdapter

    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var calendarView: CalendarView
    private lateinit var btnResetFilter: Button
    private lateinit var searchView: SearchView

    private var selectedDateFilter: String? = null
    private var searchQueryFilter: String? = null

    private var userRole: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        kegiatanList = arrayListOf()
        allKegiatanList = arrayListOf()

        initViews()
        checkUserRole()
    }
    
    // Tampilkan menu titik tiga
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_admin, menu)
        return true
    }

    // Tangani klik pada menu
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_edit -> {
                // Arahkan ke halaman pengaturan atau edit profil
                startActivity(Intent(this, PengaturanAdminActivity::class.java))
                true
            }
            R.id.menu_delete -> {
                // Arahkan ke halaman riwayat kegiatan
                startActivity(Intent(this, RiwayatadminActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        fabAdd = findViewById(R.id.fabAdd)
        calendarView = findViewById(R.id.calendarView)
        btnResetFilter = findViewById(R.id.btnResetFilter)
        searchView = findViewById(R.id.searchView)

        fabAdd.setOnClickListener { showAddKegiatanDialog() }

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDateFilter = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year)
            filterKegiatan()
        }

        btnResetFilter.setOnClickListener {
            selectedDateFilter = null
            searchQueryFilter = null
            searchView.setQuery("", false)
            searchView.clearFocus()
            calendarView.date = Calendar.getInstance().timeInMillis
            filterKegiatan()
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchQueryFilter = query?.takeIf { it.isNotEmpty() }
                filterKegiatan()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchQueryFilter = newText?.takeIf { it.isNotEmpty() }
                filterKegiatan()
                return true
            }
        })
    }

    private fun checkUserRole() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userDocRef = FirebaseFirestore.getInstance().collection("users").document(uid)

        userDocRef.get()
            .addOnSuccessListener { document ->
                userRole = document.getString("role")
                if (userRole == "user") {
                    fabAdd.hide()
                }
                setupAdapter()
                loadDataFromFirestore()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal memuat data role", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupAdapter() {
        val editClick: ((KegiatanModel) -> Unit)? =
            if (userRole == "admin") { { kegiatan -> showAddKegiatanDialog(true, kegiatan) } } else null

        val deleteClick: ((KegiatanModel) -> Unit)? =
            if (userRole == "admin") { { kegiatan -> deleteKegiatan(kegiatan) } } else null

        adapter = KegiatanAdapter(
            context = this,
            list = kegiatanList,
            onItemClick = { kegiatan ->
                val intent = Intent(this, DetailActivity::class.java).apply {
                    putExtra("judul", kegiatan.judul)
                    putExtra("deskripsi", kegiatan.deskripsi)
                    putExtra("tempat", kegiatan.tempat)
                    putExtra("tanggal", kegiatan.tanggal)
                    putExtra("waktu", kegiatan.waktu)
                    putExtra("kategori", kegiatan.kategori)
                }
                startActivity(intent)
            },
            onEditClick = editClick,
            onDeleteClick = deleteClick
        )
        recyclerView.adapter = adapter
    }


    private fun loadDataFromFirestore() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection("users").document(uid).collection("kegiatan")
            .get()
            .addOnSuccessListener { result ->
                kegiatanList.clear()
                allKegiatanList.clear()
                for (doc in result) {
                    val kegiatan = doc.toObject(KegiatanModel::class.java)
                    kegiatan.id = doc.id
                    kegiatanList.add(kegiatan)
                    allKegiatanList.add(kegiatan)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal mengambil data", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteKegiatan(kegiatan: KegiatanModel) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val kegiatanId = kegiatan.id ?: return

        FirebaseFirestore.getInstance()
            .collection("users").document(uid)
            .collection("kegiatan").document(kegiatanId)
            .delete()
            .addOnSuccessListener {
                kegiatanList.remove(kegiatan)
                allKegiatanList.remove(kegiatan)
                adapter.notifyDataSetChanged()
                Toast.makeText(this, "Kegiatan dihapus", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal menghapus data", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showAddKegiatanDialog(isEdit: Boolean = false, oldKegiatan: KegiatanModel? = null) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_kegiatan, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle(if (isEdit) "Edit Kegiatan" else "Tambah Kegiatan")
            .setPositiveButton(if (isEdit) "Update" else "Simpan", null)
            .setNegativeButton("Batal", null)
            .create()

        dialog.setOnShowListener {
            val etJudul = dialogView.findViewById<EditText>(R.id.etJudul)
            val etDeskripsi = dialogView.findViewById<EditText>(R.id.etDeskripsi)
            val etTempat = dialogView.findViewById<EditText>(R.id.etTempat)
            val etTanggal = dialogView.findViewById<EditText>(R.id.etTanggal)
            val etWaktu = dialogView.findViewById<EditText>(R.id.etWaktu)
            val spinnerKategori = dialogView.findViewById<Spinner>(R.id.spinnerKategori)

            val kategoriList = listOf("Penting", "Biasa", "Santai")
            spinnerKategori.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, kategoriList)

            if (isEdit && oldKegiatan != null) {
                etJudul.setText(oldKegiatan.judul)
                etDeskripsi.setText(oldKegiatan.deskripsi)
                etTempat.setText(oldKegiatan.tempat)
                etTanggal.setText(oldKegiatan.tanggal)
                etWaktu.setText(oldKegiatan.waktu)
                spinnerKategori.setSelection(kategoriList.indexOf(oldKegiatan.kategori))
            }

            etTanggal.setOnClickListener {
                val c = Calendar.getInstance()
                DatePickerDialog(this, { _, year, month, day ->
                    etTanggal.setText(String.format("%02d/%02d/%d", day, month + 1, year))
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
            }

            etWaktu.setOnClickListener {
                val c = Calendar.getInstance()
                TimePickerDialog(this, { _, hour, minute ->
                    etWaktu.setText(String.format("%02d:%02d", hour, minute))
                }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show()
            }

            val btnSave = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            btnSave.setOnClickListener {
                val judul = etJudul.text.toString().trim()
                val deskripsi = etDeskripsi.text.toString().trim()
                val tempat = etTempat.text.toString().trim()
                val tanggal = etTanggal.text.toString().trim()
                val waktu = etWaktu.text.toString().trim()
                val kategori = spinnerKategori.selectedItem.toString()

                if (judul.isEmpty()) {
                    Toast.makeText(this, "Judul tidak boleh kosong", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
                val firestore = FirebaseFirestore.getInstance()
                val collectionRef = firestore.collection("users").document(uid).collection("kegiatan")

                if (isEdit && oldKegiatan != null) {
                    val updated = KegiatanModel(judul, deskripsi, tempat, tanggal, waktu, kategori, oldKegiatan.id)
                    oldKegiatan.id?.let { kegiatanId ->
                        collectionRef.document(kegiatanId).set(updated)
                            .addOnSuccessListener {
                                val index = kegiatanList.indexOfFirst { it.id == kegiatanId }
                                if (index != -1) {
                                    kegiatanList[index] = updated
                                    adapter.notifyItemChanged(index)
                                }
                                val allIndex = allKegiatanList.indexOfFirst { it.id == kegiatanId }
                                if (allIndex != -1) allKegiatanList[allIndex] = updated
                                dialog.dismiss()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Gagal memperbarui kegiatan", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    val newItem = KegiatanModel(judul, deskripsi, tempat, tanggal, waktu, kategori)
                    collectionRef.add(newItem)
                        .addOnSuccessListener { docRef ->
                            newItem.id = docRef.id
                            kegiatanList.add(newItem)
                            allKegiatanList.add(newItem)
                            adapter.notifyItemInserted(kegiatanList.size - 1)
                            dialog.dismiss()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Gagal menyimpan kegiatan", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }
        dialog.show()
    }

    private fun filterKegiatan() {
        val filtered = allKegiatanList.filter { kegiatan ->
            val matchDate = selectedDateFilter?.let { kegiatan.tanggal == it } ?: true
            val matchSearch = searchQueryFilter?.let {
                kegiatan.judul.contains(it, ignoreCase = true) ||
                        kegiatan.deskripsi.contains(it, ignoreCase = true)
            } ?: true
            matchDate && matchSearch
        }

        kegiatanList.clear()
        kegiatanList.addAll(filtered)
        adapter.notifyDataSetChanged()
    }
}
