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


    private var selectedDateFilter: String? = null
    private var searchQueryFilter: String? = null

    private var userRole: String? = null
    private var currentUserUid: String? = null

    private var menuSearchView: SearchView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""

        kegiatanList = arrayListOf()
        allKegiatanList = arrayListOf()

        initViews()
        checkUserRole()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_admin, menu)

        val searchItem = menu?.findItem(R.id.menu_search)
        menuSearchView = searchItem?.actionView as? SearchView

        menuSearchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
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

        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_edit -> {
                startActivity(Intent(this, PengaturanAdminActivity::class.java))
                true
            }
            R.id.menu_delete -> {
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


        fabAdd.setOnClickListener { showAddKegiatanDialog() }

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDateFilter = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year)
            filterKegiatan()
        }

        btnResetFilter.setOnClickListener {
            selectedDateFilter = null
            searchQueryFilter = null
            menuSearchView?.setQuery("", false)
            menuSearchView?.clearFocus()
            calendarView.date = Calendar.getInstance().timeInMillis
            filterKegiatan()
        }


    }

    private fun checkUserRole() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        currentUserUid = uid
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
        val uid = currentUserUid ?: return
        val kegiatanRef = if (userRole == "admin") {
            FirebaseFirestore.getInstance().collectionGroup("kegiatan")
        } else {
            FirebaseFirestore.getInstance().collection("users").document(uid).collection("kegiatan")
        }

        kegiatanRef.get()
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
        val uid = currentUserUid ?: return
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

    // ... showAddKegiatanDialog dan filterKegiatan tetap tidak berubah
    private fun showAddKegiatanDialog(isEdit: Boolean = false, kegiatan: KegiatanModel? = null) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_kegiatan, null)
        val etJudul = dialogView.findViewById<EditText>(R.id.etJudul)
        val etDeskripsi = dialogView.findViewById<EditText>(R.id.etDeskripsi)
        val etTempat = dialogView.findViewById<EditText>(R.id.etTempat)
        val etTanggal = dialogView.findViewById<EditText>(R.id.etTanggal)
        val etWaktu = dialogView.findViewById<EditText>(R.id.etWaktu)
        val spinnerKategori = dialogView.findViewById<Spinner>(R.id.spinnerKategori)

        // Siapkan daftar kategori
        val kategoriList = arrayOf("Kegiatan Rutin", "Rapat", "Pelatihan", "Kunjungan")
        val kategoriAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, kategoriList)
        spinnerKategori.adapter = kategoriAdapter

        // Jika sedang edit, isi field dengan data kegiatan
        if (isEdit && kegiatan != null) {
            etJudul.setText(kegiatan.judul)
            etDeskripsi.setText(kegiatan.deskripsi)
            etTempat.setText(kegiatan.tempat)
            etTanggal.setText(kegiatan.tanggal)
            etWaktu.setText(kegiatan.waktu)
            val selectedIndex = kategoriList.indexOf(kegiatan.kategori)
            if (selectedIndex >= 0) spinnerKategori.setSelection(selectedIndex)
        }

        // Date picker untuk tanggal
        etTanggal.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePicker = DatePickerDialog(
                this,
                { _, year, month, day ->
                    etTanggal.setText(String.format("%02d/%02d/%d", day, month + 1, year))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }

        // Time picker untuk waktu
        etWaktu.setOnClickListener {
            val calendar = Calendar.getInstance()
            val timePicker = TimePickerDialog(
                this,
                { _, hour, minute ->
                    etWaktu.setText(String.format("%02d:%02d", hour, minute))
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            )
            timePicker.show()
        }

        // Buat dialog
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle(if (isEdit) "Edit Kegiatan" else "Tambah Kegiatan")
            .setPositiveButton("Simpan", null)
            .setNegativeButton("Batal", null)
            .create()

        dialog.setOnShowListener {
            val button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            button.setOnClickListener {
                val judul = etJudul.text.toString().trim()
                val deskripsi = etDeskripsi.text.toString().trim()
                val tempat = etTempat.text.toString().trim()
                val tanggal = etTanggal.text.toString().trim()
                val waktu = etWaktu.text.toString().trim()
                val kategori = spinnerKategori.selectedItem.toString().trim()

                // Validasi
                if (judul.isEmpty() || deskripsi.isEmpty() || tempat.isEmpty() ||
                    tanggal.isEmpty() || waktu.isEmpty() || kategori.isEmpty()
                ) {
                    Toast.makeText(this, "Semua kolom harus diisi", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val uid = currentUserUid ?: return@setOnClickListener
                val firestore = FirebaseFirestore.getInstance()
                val kegiatanData = hashMapOf(
                    "judul" to judul,
                    "deskripsi" to deskripsi,
                    "tempat" to tempat,
                    "tanggal" to tanggal,
                    "waktu" to waktu,
                    "kategori" to kategori
                )

                if (isEdit && kegiatan != null) {
                    firestore.collection("users").document(uid)
                        .collection("kegiatan").document(kegiatan.id!!)
                        .set(kegiatanData)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Kegiatan diperbarui", Toast.LENGTH_SHORT).show()
                            loadDataFromFirestore()
                            dialog.dismiss()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Gagal memperbarui kegiatan", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    firestore.collection("users").document(uid)
                        .collection("kegiatan").add(kegiatanData)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Kegiatan ditambahkan", Toast.LENGTH_SHORT).show()
                            loadDataFromFirestore()
                            dialog.dismiss()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Gagal menambahkan kegiatan", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }

        dialog.show()
    }

    private fun filterKegiatan() {
        kegiatanList.clear()
        for (kegiatan in allKegiatanList) {
            val matchDate = selectedDateFilter == null || kegiatan.tanggal == selectedDateFilter
            val matchQuery = searchQueryFilter == null ||
                    kegiatan.judul.contains(searchQueryFilter!!, ignoreCase = true) ||
                    kegiatan.deskripsi.contains(searchQueryFilter!!, ignoreCase = true)

            if (matchDate && matchQuery) {
                kegiatanList.add(kegiatan)
            }
        }
        adapter.notifyDataSetChanged()
    }

}
