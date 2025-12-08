package hu.bme.ait.sean.data

data class StoredAlbumData (
    val name : String = "",
    val artist : String = "",
    val img_url : String = ""
)

data class StoredAlbumDataID (
    val albumData: StoredAlbumData,
    val id : String
)