package com.example.dp3akbpenjadwalan

import android.app.AlertDialog
import android.content.Context
import android.view.*
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.dp3akbpenjadwalan.model.KegiatanModel

class KegiatanAdapter(
    private val context: Context,
    private val list: List<KegiatanModel>,
    private val onItemClick: (KegiatanModel) -> Unit,
    private val onEditClick: (KegiatanModel) -> Unit,
    private val onDeleteClick: (KegiatanModel) -> Unit
) : RecyclerView.Adapter<KegiatanAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val judul = itemView.findViewById<TextView>(R.id.tvJudul)
        private val deskripsi = itemView.findViewById<TextView>(R.id.tvDeskripsi)
        private val tempat = itemView.findViewById<TextView>(R.id.tvTempat)
        private val tanggal = itemView.findViewById<TextView>(R.id.tvTanggal)
        private val waktu = itemView.findViewById<TextView>(R.id.tvWaktu)
        private val kategori = itemView.findViewById<TextView>(R.id.tvKategori)
        private val ivMenu = itemView.findViewById<ImageView>(R.id.ivMenu)

        fun bind(item: KegiatanModel) {
            judul.text = item.judul
            deskripsi.text = item.deskripsi
            tempat.text = item.tempat
            tanggal.text = item.tanggal
            waktu.text = item.waktu
            kategori.text = item.kategori

            itemView.setOnClickListener { onItemClick(item) }

            ivMenu.setOnClickListener { v ->
                val popup = PopupMenu(context, v)
                popup.inflate(R.menu.menu_card_item)
                popup.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.menu_edit -> {
                            onEditClick(item)
                            true
                        }
                        R.id.menu_delete -> {
                            AlertDialog.Builder(context)
                                .setTitle("Hapus Kegiatan")
                                .setMessage("Yakin ingin menghapus kegiatan ini?")
                                .setPositiveButton("Hapus") { _, _ ->
                                    onDeleteClick(item)
                                    Toast.makeText(context, "Kegiatan dihapus", Toast.LENGTH_SHORT).show()
                                }
                                .setNegativeButton("Batal", null)
                                .show()
                            true
                        }
                        else -> false
                    }
                }
                popup.show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_kegiatan, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size
}
