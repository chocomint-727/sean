package hu.bme.ait.sean.data

data class StoredAlbumData (
    val name: String = "",
    val artist: String = "",
    val img_url: String = "",
    val reviewUids: List<String> = emptyList()
)

data class StoredUID (
    val uid : String
)

data class StoredAlbumDataID (
    val albumData: StoredAlbumData = StoredAlbumData(),
    val id : String = ""
)
