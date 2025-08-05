package com.example.dp3akbpenjadwalan

import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit
import KegiatanModel
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*


class DashboardActivity : AppCompatActivity() {

    // Daftar kegiatan yang akan ditampilkan
    private lateinit var kegiatanList: ArrayList<KegiatanModel>
    private lateinit var allKegiatanList: ArrayList<KegiatanModel> // Untuk kebutuhan filter

    // Adapter untuk RecyclerView
    private lateinit var adapter: KegiatanAdapter

    // Komponen UI
    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var calendarView: CalendarView
    private lateinit var btnResetFilter: Button

    // Filter dan user role
    private val role: String = "pegawai"
    private var selectedDateFilter: String? = null
    private var searchQueryFilter: String? = null
    private var userRole: String? = null
    private var currentUserUid: String? = null
    private var menuSearchView: SearchView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dashboard)

        //  untuk Android 13+ (izin notifikasi)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    100
                )
            }
        }

        // Atur Toolbar
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""

        // Inisialisasi list
        kegiatanList = arrayListOf()
        allKegiatanList = arrayListOf()

        // Inisialisasi tampilan & cek role user
        initViews()
        checkUserRole()
    }
    // Callback saat permintaan izin selesai
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 100) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Izin diberikan, aman
            } else {
                Toast.makeText(this, "Izin notifikasi ditolak. Notifikasi tidak bisa ditampilkan.", Toast.LENGTH_SHORT).show()
            }
        }
    }


    // Menambahkan menu (search dan pengaturan)
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_admin, menu)

        val searchItem = menu?.findItem(R.id.menu_search)
        menuSearchView = searchItem?.actionView as? SearchView

        // Atur listener untuk search
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

    // Handle klik item menu
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_Pengaturan -> {
                startActivity(Intent(this, PengaturanAdminActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // Inisialisasi semua view di layout
    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        fabAdd = findViewById(R.id.fabAdd)
        calendarView = findViewById(R.id.calendarView)
        btnResetFilter = findViewById(R.id.btnResetFilter)

        // Tambah kegiatan
        fabAdd.setOnClickListener { showAddKegiatanDialog() }

        // Filter berdasarkan tanggal
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDateFilter = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year)
            filterKegiatan()
        }
        // tombol Reset semua filter
        btnResetFilter.setOnClickListener {
            selectedDateFilter = null
            searchQueryFilter = null
            menuSearchView?.setQuery("", false)
            menuSearchView?.clearFocus()
            calendarView.date = Calendar.getInstance().timeInMillis
            filterKegiatan()
        }


    }
    // Mengecek apakah user adalah admin atau bukan
    private fun checkUserRole() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        currentUserUid = uid
        val userDocRef = FirebaseFirestore.getInstance().collection("users").document(uid)

        userDocRef.get()
            .addOnSuccessListener { document ->
                userRole = document.getString("role")
                val isAdmin = document.getBoolean("isAdmin") ?: false

                if (!isAdmin) {
                    fabAdd.hide() // Sembunyikan tombol tambah jika bukan admin
                }

                // Jika ingin pakai peran spesifik juga
                Log.d("RoleDebug", "Role: $userRole | IsAdmin: $isAdmin")

                setupAdapter(isAdmin)
                loadDataFromFirestore()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal memuat data role", Toast.LENGTH_SHORT).show()
            }
    }

    // Inisialisasi adapter untuk RecyclerView
    private fun setupAdapter(isAdmin: Boolean) {
        val editClick: ((KegiatanModel) -> Unit)? =
            if (isAdmin) { { kegiatan -> showAddKegiatanDialog(true, kegiatan) } } else null

        val deleteClick: ((KegiatanModel) -> Unit)? =
            if (isAdmin) { { kegiatan -> deleteKegiatan(kegiatan) } } else null

        adapter = KegiatanAdapter(
            context = this,
            list = kegiatanList,
            onItemClick = { kegiatan ->
                // Buka DetailActivity saat item diklik
                val intent = Intent(this, DetailActivity::class.java).apply {
                    putExtra("judul", kegiatan.judul)
                    putExtra("deskripsi", kegiatan.deskripsi)
                    putExtra("tempat", kegiatan.tempat)
                    putExtra("tanggal", kegiatan.tanggal)
                    putExtra("waktu", kegiatan.waktu)
                    putExtra("kategori", kegiatan.kategori)
                    putExtra("bidang", kegiatan.bidang)
                }
                startActivity(intent)
            },
            onEditClick = editClick,
            onDeleteClick = deleteClick,
            role = if (isAdmin) "admin" else "user" // kirimkan sebagai string ke adapter
        )
        recyclerView.adapter = adapter
    }

    // Ambil data kegiatan dari Firestore
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

    // Menghapus kegiatan dari Firestore
    private fun deleteKegiatan(kegiatan: KegiatanModel) {
        val kegiatanId = kegiatan.id ?: return
        val ownerUid = kegiatan.uid ?: return

        val firestore = FirebaseFirestore.getInstance()
        val userRef = firestore.collection("users").document(ownerUid).collection("kegiatan").document(kegiatanId)
        val globalRef = firestore.collection("kegiatan").document(kegiatanId)

        // Hapus dari kedua koleksi
        userRef.delete()
            .addOnSuccessListener {
                globalRef.delete()
                    .addOnSuccessListener {
                        kegiatanList.remove(kegiatan)
                        allKegiatanList.remove(kegiatan)
                        adapter.notifyDataSetChanged()
                        Toast.makeText(this, "Kegiatan dihapus", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Gagal menghapus dari koleksi global", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal menghapus dari koleksi user", Toast.LENGTH_SHORT).show()
            }
    }



    // Menampilkan dialog tambah/edit kegiatan
    private fun showAddKegiatanDialog(isEdit: Boolean = false, kegiatan: KegiatanModel? = null) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_kegiatan, null)
        // Ambil referensi ke input dan spinner
        val etJudul = dialogView.findViewById<EditText>(R.id.etJudul)
        val etDeskripsi = dialogView.findViewById<EditText>(R.id.etDeskripsi)
        val etTempat = dialogView.findViewById<EditText>(R.id.etTempat)
        val etTanggal = dialogView.findViewById<EditText>(R.id.etTanggal)
        val etWaktu = dialogView.findViewById<EditText>(R.id.etWaktu)
        val spinnerKategori = dialogView.findViewById<Spinner>(R.id.spinnerKategori)

        // Isi spinner kategori & bidang
        val kategoriList = arrayOf("Kegiatan Rutin", "Rapat", "Pelatihan", "Kunjungan")
        val kategoriAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, kategoriList)
        spinnerKategori.adapter = kategoriAdapter

       // Spinner Bidang
        val spinnerBidang = dialogView.findViewById<Spinner>(R.id.spinnerBidang)
        // Batasi pilihan bidang sesuai role admin
        val bidangList = arrayOf("PP", "PPA", "PUG", "KB", "UPTD PPA")
        val bidangAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, bidangList)
        spinnerBidang.adapter = bidangAdapter

        // Jika userRole sesuai salah satu bidang, nonaktifkan pilihan lain
        userRole?.let { role ->
            val bidangDariRole = role.replace("Kabid ", "").trim() // contoh: "Kabid PUG" -> "PUG"
            if (bidangDariRole in bidangList) {
                val index = bidangList.indexOf(bidangDariRole)
                spinnerBidang.setSelection(index)
                spinnerBidang.isEnabled = false
            }
        }


        // Jika mode edit, tampilkan data sebelumnya
        if (isEdit && kegiatan != null) {
            etJudul.setText(kegiatan.judul)
            etDeskripsi.setText(kegiatan.deskripsi)
            etTempat.setText(kegiatan.tempat)
            etTanggal.setText(kegiatan.tanggal)
            etWaktu.setText(kegiatan.waktu)
            val selectedIndex = kategoriList.indexOf(kegiatan.kategori)
            if (selectedIndex >= 0) spinnerKategori.setSelection(selectedIndex)
        }

        // Pilih tanggal dan waktu menggunakan picker
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

        // Buat dan tampilkan dialog
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle(if (isEdit) "Edit Kegiatan" else "Tambah Kegiatan")
            .setPositiveButton("Simpan", null)
            .setNegativeButton("Batal", null)
            .create()

        dialog.setOnShowListener {
            val button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            button.setOnClickListener {
                // Ambil data input
                val judul = etJudul.text.toString().trim()
                val deskripsi = etDeskripsi.text.toString().trim()
                val tempat = etTempat.text.toString().trim()
                val tanggal = etTanggal.text.toString().trim()
                val waktu = etWaktu.text.toString().trim()
                val kategori = spinnerKategori.selectedItem.toString().trim()
                val bidang = spinnerBidang.selectedItem.toString().trim()


                // Validasi input
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
                    "kategori" to kategori,
                    "bidang" to bidang,
                    "uid" to uid
                )
                // Mode edit: update data lama
                if (isEdit && kegiatan != null) {
                    val ownerUid = kegiatan.uid ?: return@setOnClickListener
                    val kegiatanId = kegiatan.id ?: return@setOnClickListener

                    val userRef = firestore.collection("users").document(ownerUid)
                        .collection("kegiatan").document(kegiatanId)

                    val globalRef = firestore.collection("kegiatan").document(kegiatanId)

                    // Update kedua lokasi: user dan global
                    userRef.set(kegiatanData)
                    globalRef.set(kegiatanData)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Kegiatan diperbarui", Toast.LENGTH_SHORT).show()
                            loadDataFromFirestore()
                            dialog.dismiss()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Gagal memperbarui kegiatan", Toast.LENGTH_SHORT).show()
                        }
                }

                else {
                    // Tambah kegiatan baru
                val globalRef = firestore.collection("kegiatan").document()
                val userRef = firestore.collection("users").document(uid)
                    .collection("kegiatan").document(globalRef.id)

                // Tambahkan ID-nya ke data juga
                kegiatanData["id"] = globalRef.id
                kegiatanData["uid"] = uid

                // Simpan ke koleksi global
                globalRef.set(kegiatanData)
                    .addOnSuccessListener {
                        // Simpan juga ke koleksi user
                        userRef.set(kegiatanData)

                        // Tambahkan pemanggilan notifikasi di sini
                        scheduleKegiatanReminder(this, KegiatanModel(
                            id = globalRef.id,
                            uid = uid,
                            judul = judul,
                            deskripsi = deskripsi,
                            tempat = tempat,
                            tanggal = tanggal,
                            waktu = waktu,
                            kategori = kategori
                        ))


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

    // Filter kegiatan berdasarkan tanggal dan pencarian
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
    // Jadwalkan pengingat kegiatan (notifikasi)
    private fun scheduleKegiatanReminder(context: Context, kegiatan: KegiatanModel) {
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val date = formatter.parse(kegiatan.tanggal)

        date?.let {
            val calendar = Calendar.getInstance().apply {
                time = it
                add(Calendar.DAY_OF_YEAR, -1) // Reminder 1 hari sebelumnya
            }

            val intent = Intent(context.applicationContext, KegiatanReminderReceiver::class.java).apply {
                putExtra("judul", kegiatan.judul)
                putExtra("deskripsi", kegiatan.deskripsi)
                putExtra("tanggal", kegiatan.tanggal)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context.applicationContext,
                kegiatan.id.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            alarmManager?.set(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }








}
