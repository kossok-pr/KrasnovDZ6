package com.example.android.krasnovdz6

import android.app.Activity
import android.graphics.BitmapFactory
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_fullscreen_image.*

class FullscreenImageActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fullscreen_image)

        val img = intent.getStringExtra("image")
        fullscreen_img.setImageBitmap(BitmapFactory.decodeFile(img))
    }
}
