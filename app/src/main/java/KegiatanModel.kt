import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
data class KegiatanModel(
    var id: String? = null,
    var uid: String? = null,
    var judul: String = "",
    var deskripsi: String = "",
    var tempat: String = "",
    var tanggal: String = "",
    var waktu: String = "",
    var kategori: String = ""
) : Parcelable

