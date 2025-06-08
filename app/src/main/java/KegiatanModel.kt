import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class KegiatanModel(
    var judul: String = "",
    var deskripsi: String = "",
    var tempat: String = "",
    var tanggal: String = "",
    var waktu: String = "",
    var kategori: String = "",
    var id: String? = null
) : Parcelable
