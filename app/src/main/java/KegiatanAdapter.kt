package com.example.dp3akbpenjadwalan

import KegiatanModel
import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView


// Adapter untuk menampilkan daftar kegiatan
class KegiatanAdapter(
    private val context: Context, // Konteks activity
    private val list: List<KegiatanModel>, // List data kegiatan
    private val onItemClick: (KegiatanModel) -> Unit, // Fungsi saat item diklik (lihat detail)
    private val onEditClick: ((KegiatanModel) -> Unit)? = null, // Fungsi edit (opsional, hanya admin)
    private val onDeleteClick: ((KegiatanModel) -> Unit)? = null, // Fungsi hapus (opsional, hanya admin)
    private val role: String // Role user: admin atau pegawai
) : RecyclerView.Adapter<KegiatanAdapter.ViewHolder>() {

    // ViewHolder: menyimpan referensi ke view item_kegiatan.xml
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val judul = itemView.findViewById<TextView>(R.id.tvJudul)
        private val deskripsi = itemView.findViewById<TextView>(R.id.tvDeskripsi)
        private val tempat = itemView.findViewById<TextView>(R.id.tvTempat)
        private val tanggal = itemView.findViewById<TextView>(R.id.tvTanggal)
        private val waktu = itemView.findViewById<TextView>(R.id.tvWaktu)
        private val kategori = itemView.findViewById<TextView>(R.id.tvKategori)
        private val ivMenu = itemView.findViewById<ImageView>(R.id.ivMenu)  // Ikon tiga titik (opsi)
        private val bidang = itemView.findViewById<TextView>(R.id.tvBidang)

        // Fungsi untuk mengikat data kegiatan ke tampilan
        fun bind(item: KegiatanModel) {
            Log.d("KegiatanAdapter", "Role: $role")
            // Set teks pada tiap TextView
            judul.text = item.judul
            deskripsi.text = item.deskripsi
            tempat.text = item.tempat
            tanggal.text = item.tanggal
            waktu.text = item.waktu
            kategori.text = item.kategori
            bidang.text = item.bidang

            // Saat item diklik, jalankan fungsi yang dikirim dari luar (lihat detail)
            itemView.setOnClickListener { onItemClick(item) }

            // Tampilkan tombol menu hanya jika role-nya admin
            if (role == "admin"){
                ivMenu.visibility = View.VISIBLE    // Tampilkan ikon tiga titik
                // Ketika ikon tiga titik diklik
                ivMenu.setOnClickListener { v ->
                    val popup = PopupMenu(context, v)
                    popup.inflate(R.menu.menu_card_item)     // Menu edit/hapus
                    // Handle klik menu
                    popup.setOnMenuItemClickListener { menuItem ->
                        when (menuItem.itemId) {
                            R.id.menu_edit -> {
                                // Panggil fungsi edit jika ada
                                onEditClick?.invoke(item)
                                true
                            }
                            R.id.menu_delete -> {
                                // Tampilkan dialog konfirmasi hapus
                                AlertDialog.Builder(context)
                                    .setTitle("Hapus Kegiatan")
                                    .setMessage("Yakin ingin menghapus kegiatan ini?")
                                    .setPositiveButton("Hapus") { _, _ ->
                                        onDeleteClick?.invoke(item) // Panggil fungsi hapus jika diset
                                        Toast.makeText(context, "Kegiatan dihapus", Toast.LENGTH_SHORT).show()
                                    }
                                    .setNegativeButton("Batal", null)
                                    .show()
                                true
                            }
                            else -> false
                        }
                    }
                    popup.show()    // Tampilkan popup menu
                }
            } else {
                // Jika bukan admin, sembunyikan ikon tiga titik
                ivMenu.visibility = View.GONE
            }
        }

    }
    // Membuat view baru untuk setiap item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_kegiatan, parent, false)
        return ViewHolder(view)
    }
    // Menghubungkan data ke view holder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position]) // Panggil fungsi bind untuk setiap item
        Log.d("KegiatanAdapter", "Role di onBindViewHolder: $role")

    }
    // Jumlah item dalam list
    override fun getItemCount(): Int = list.size
}
