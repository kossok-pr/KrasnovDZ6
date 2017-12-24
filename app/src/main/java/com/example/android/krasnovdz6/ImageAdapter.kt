package com.example.android.krasnovdz6

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.*
import kotlinx.android.synthetic.main.item.view.*
import java.io.File

class ImageAdapter(private var context: Context, private var imageArray: ArrayList<File>) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    private var selectionMode = false

    private lateinit var mActionMode: ActionMode

    private var selectedItems = ArrayList<File>()

    override fun getItemCount() = imageArray.size

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val image: Bitmap? = BitmapFactory.decodeFile(imageArray.get(position).absolutePath)
        holder.image.setImageBitmap(image)

        holder.image.setOnClickListener {
            if (selectionMode == false) {
                showFullSize(imageArray.get(position).absolutePath)
            } else if (holder.checkbox.visibility == View.VISIBLE) {
                uncheckItem(holder, position)
            } else {
                checkItem(holder, position)
            }
        }

        holder.image.setOnLongClickListener {
            startSelectionMode(holder, position)
            true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ImageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item, parent, false))

    private fun showFullSize(image: String?) {
        val fullsizeIntent = Intent(context, FullscreenImageActivity::class.java)
        fullsizeIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        fullsizeIntent.putExtra("image", image)
        context.startActivity(fullsizeIntent)
    }

    private fun checkItem(holder: ImageViewHolder, position: Int) {
        holder.checkbox.visibility = View.VISIBLE
        holder.checkbox.isChecked = true
        selectedItems.add(imageArray.get(position))
    }

    private fun uncheckItem(holder: ImageViewHolder, position: Int) {
        holder.checkbox.visibility = View.GONE
        selectedItems.remove(imageArray.get(position))
        if (selectedItems.isEmpty()) mActionMode.finish()
    }

    private fun startSelectionMode(holder: ImageViewHolder, position: Int) {
        selectionMode = true
        holder.checkbox.visibility = View.VISIBLE
        holder.checkbox.isChecked = true
        val appCompat = context as AppCompatActivity
        mActionMode = appCompat.startActionMode(actionBar)
        selectedItems.add(imageArray.get(position))
    }

    class ImageViewHolder(var view: View) : RecyclerView.ViewHolder(view) {
        var image: ImageView = view.item_img
        var checkbox: CheckBox = view.item_checkbox
    }

    var actionBar = object: ActionMode.Callback {

        override fun onActionItemClicked(p0: ActionMode, p1: MenuItem): Boolean {
            when (p1.itemId) {
                R.id.context_menu_delete_btn -> { deleteImages(); return true }
                R.id.context_menu_rename_btn -> { renameImage(); return true }
                R.id.context_menu_share_btn -> { shareImage(); return true }
                else -> return false
            }
        }

        override fun onCreateActionMode(p0: ActionMode, p1: Menu): Boolean {
            p0.menuInflater.inflate(R.menu.context_menu, p1)
            return true
        }

        override fun onPrepareActionMode(p0: ActionMode, p1: Menu): Boolean {
            return false
        }

        override fun onDestroyActionMode(p0: ActionMode) {
            selectionMode = false
        }

        private fun deleteImages() {
            if (selectedItems.isEmpty()) Toast.makeText(context, "Select items to delete", Toast.LENGTH_SHORT).show()
            else for (f: File in selectedItems) {
                val removeIndex = imageArray.indexOf(f)
                f.delete()
                notifyItemRemoved(removeIndex)
                notifyItemRangeChanged(removeIndex, imageArray.size)
                imageArray.remove(f)
            }
            selectionMode = false
            mActionMode.finish()
            selectedItems.clear()
        }

        private fun renameImage() {
            if (selectedItems.size > 1) Toast.makeText(context, "You can rename only one image", Toast.LENGTH_SHORT).show()
            else {
                val file = selectedItems.get(0)
                val inflater = context.getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val popupView = inflater.inflate(R.layout.rename_popup_window, (context as AppCompatActivity).findViewById(R.id.rename_popup_root))
                val popupBtn: Button = popupView.findViewById(R.id.rename_popup_done_btn)
                val popupEditText: EditText = popupView.findViewById(R.id.rename_popup_edit_text)
                popupEditText.setText(file.name)
                val popupWindow = PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)
                popupWindow.showAtLocation((context as AppCompatActivity).findViewById(R.id.main_content), Gravity.CENTER, 0, 0)
                popupBtn.setOnClickListener {
                    val text = popupEditText.text.toString()
                    if (!text.isEmpty()) {
                        if (selectedItems.get(0).renameTo(File(file.parent + "/" + text)) == true) {
                            deleteImages()
                            popupWindow.dismiss()
                            notifyDataSetChanged()
                        }
                    } else Toast.makeText(context, "Enter new name", Toast.LENGTH_SHORT).show()
                }
            }
        }

        private fun shareImage() {
            val imageUrisList = ArrayList<Uri>()
            for (f: File in selectedItems) {
                val uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", f)
                imageUrisList.add(uri)
            }
            val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE)
            shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUrisList)
            shareIntent.setType("image/*")
            context.startActivity(Intent.createChooser(shareIntent, "Share images to.."))
            selectedItems.clear()
        }
    }
}