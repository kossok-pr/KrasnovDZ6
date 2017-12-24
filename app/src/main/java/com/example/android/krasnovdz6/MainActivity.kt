package com.example.android.krasnovdz6

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private val imageArray = ArrayList<File>()

    private lateinit var mAdapter: ImageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val DCIMDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
        val DCIMArray = DCIMDirectory.listFiles()

        val picturesDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val picturesArray = picturesDirectory.listFiles()

        val commonArray = ArrayList<File>()
        try {
            commonArray.addAll(DCIMArray)
            commonArray.addAll(picturesArray)
        } catch (e: IllegalStateException) {
            Toast.makeText(applicationContext, "Nothing to show", Toast.LENGTH_SHORT).show()
        }

        fillImageArray(commonArray)
        sortImageArray()

        img_list.layoutManager = GridLayoutManager(applicationContext, 2)
        mAdapter = ImageAdapter(this, imageArray)
        img_list.adapter = mAdapter

        take_photo_btn.setOnClickListener {
            takePhoto()
        }

        upload_btn.setOnClickListener {
            uploadImage()
        }

        search_btn.setOnClickListener {
            val allPicturesIntent = Intent(this, AllPicturesActivity::class.java)
            startActivity(allPicturesIntent)
        }
    }

    private fun fillImageArray(commonArray: ArrayList<File>) {
        for (f: File in commonArray) {
            if ((f.isFile) && ImageTypeIdentifier(f.absolutePath).isImage()) imageArray.add(f)
            else if (f.isDirectory) {
                val tempArray = ArrayList<File>()
                tempArray.addAll(f.listFiles())
                fillImageArray(tempArray)
                continue
            }
        }
    }

    private fun sortImageArray() {
        for (i in 0 until imageArray.size) {
            var latest = imageArray[i].lastModified()
            var latest_i = i
            for (j in (i + 1) until imageArray.size) {
                if (imageArray[j].lastModified() > latest) {
                    latest = imageArray[j].lastModified()
                    latest_i = j
                }
            }
            if (i != latest_i) {
                val tmp = imageArray[i]
                imageArray[i] = imageArray[latest_i]
                imageArray[latest_i] = tmp
            }
        }
    }

    private fun takePhoto() {
        val parentPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() + "/dz6"
        if (!File(parentPath).exists()) File(parentPath).mkdir()
        val takePhoto = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val image = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() + "/dz6",
                "IMG_" + SimpleDateFormat("yyyyMMdd_HHmmss").format(Date()) + ".jpg")
        takePhoto.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(applicationContext,
                BuildConfig.APPLICATION_ID + ".provider", image))
        startActivityForResult(takePhoto, 1)
        imageArray.add(0, image)
        mAdapter.notifyItemInserted(0)
    }

    private fun uploadImage() {
        link_enter.visibility = View.VISIBLE
        ok_btn.visibility = View.VISIBLE
        ok_btn.setOnClickListener {
            val link = link_enter.text.toString()
            if (ImageTypeIdentifier(link).isImage()) UploadTask().execute(link)
            else Toast.makeText(applicationContext, "This is not an image", Toast.LENGTH_SHORT).show()
            link_enter.visibility = View.GONE
            ok_btn.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
    }

    inner class UploadTask : AsyncTask<String, Void, Bitmap>() {
        override fun doInBackground(vararg p0: String?): Bitmap {
            var link = p0[0]
            link = link!!.substring(link.lastIndexOf('/') + 1)
            val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), link)
            file.createNewFile()
            val fos = FileOutputStream(file)
            val url = URL(p0[0])
            fos.write(url.readBytes())
            fos.close()

            return BitmapFactory.decodeFile(file.absolutePath)
        }
    }
}
