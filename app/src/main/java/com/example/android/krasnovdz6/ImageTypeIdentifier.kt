package com.example.android.krasnovdz6

class ImageTypeIdentifier(private var link: String) {

    val JPEG = ".jpg"
    val PNG = ".png"
    val BMP = ".bmp"
    val GIF = ".gif"

    fun isImage(): Boolean {
        link = link.toLowerCase()
        if (link.endsWith(JPEG) || link.endsWith(PNG) || link.endsWith(BMP) || link.endsWith(GIF)) return true
        else return false
    }

    fun identifyType(): String {
        link = link.toLowerCase()
        var type = "none"
        if (link.endsWith(JPEG)) type = JPEG
        if (link.endsWith(PNG)) type = PNG
        if (link.endsWith(BMP)) type = BMP
        if (link.endsWith(GIF)) type = GIF
        return type
    }
}