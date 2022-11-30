package com.example.image_picker.activity

import android.content.ContentUris
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.image_picker.R
import com.example.image_picker.model.Image
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

interface OnSelectedChange{
    fun onSelect(position: Int)
}
class SelectImageActivity : AppCompatActivity(), OnSelectedChange {
    lateinit var recyclerView: RecyclerView
    lateinit var btn_cancel: Button
    lateinit var btn_confirm: Button
    lateinit var adapter: GridAdapter
    var selected = mutableListOf<Image>()
    var address = arrayListOf<Image>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_image)
        recyclerView = findViewById(R.id.recyclerView)
        btn_cancel = findViewById<Button>(R.id.cancel_select_image_button)
        btn_confirm = findViewById<Button>(R.id.confirm_select_image_button)
        btn_cancel.setOnClickListener{
            finish()
        }
        btn_confirm.setOnClickListener{
            val data = Intent()
            var temp = mutableListOf<String>()
            selected.forEach{

            }
            temp.addAll(selected.map {it.uri })
            data.putStringArrayListExtra("Image", temp as ArrayList<String>)
            setResult(RESULT_OK, data)
            finish()
        }
        recyclerView.layoutManager = GridLayoutManager(this,3)
        adapter = GridAdapter(this,selected)
        recyclerView.adapter = adapter
        getMediaData()
        Log.i("TEST","getMediaData End")
    }
    private fun getMediaData() {
        Log.i("TEST","getMediaData Start")
        GlobalScope.launch(Dispatchers.IO){
            val videoList = mutableListOf<Image>()
            val collection =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
                } else {
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }

            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.WIDTH,
                MediaStore.Images.Media.HEIGHT,
            )

            val selection = (MediaStore.Images.Media.MIME_TYPE + "=? or "
                    + MediaStore.Images.Media.MIME_TYPE + "=?")
            val selectionArgs = arrayOf("image/jpeg", "image/png")

            val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} ASC"
            Log.i("TEST","getMediaData Query Start")
           async {
               val query = applicationContext.contentResolver.query(
                   collection,
                   projection,
                   selection,
                   selectionArgs,
                   sortOrder
               )
               query?.use { cursor ->
                   val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                   val heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)
                   val widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
                   while (cursor.moveToNext()) {
                       val id = cursor.getLong(idColumn)
                       val height = cursor.getLong(heightColumn).toDouble()
                       val width = cursor.getLong(widthColumn).toDouble()
                       val contentUri: Uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                       videoList += Image(contentUri.toString(),height,width)
                   }
               }
               Log.i("TEST","getMediaData Query End")
               runOnUiThread {
                   Log.e("============>","${videoList.size}")
                   address.addAll(videoList)
                   adapter.notifyDataSetChanged()
               }
           }
            Log.i("TEST","getMediaData Function End")
        }

    }


    inner class GridAdapter(var onChange: OnSelectedChange, temp:List<Image>): RecyclerView.Adapter<ViewHolder>() {
        private var selected:MutableList<Image> = temp.toMutableList()

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            var type = ImageView.ScaleType.FIT_CENTER
            holder.imageView.scaleType = type
            holder.checkBox.setImageResource(R.drawable.check)
            if(selected.contains(address[position])){
                holder.checkBox.setImageResource(R.drawable.checked)
            }
            holder.checkBox.setOnClickListener{
                if(selected.contains(address[position])){
                    selected.remove(address[position])
                    holder.checkBox.setImageResource(R.drawable.check)
                }else{
                    selected.add(address[position])
                    holder.checkBox.setImageResource(R.drawable.checked)
                }
                onChange.onSelect(position)
            }
            Glide.with(applicationContext).load(Uri.parse(address[position].uri)).apply(
                RequestOptions.fitCenterTransform()).into(holder.imageView);
        }

        override fun getItemCount(): Int {
            return address.size
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v: View = LayoutInflater.from(parent.context).inflate(R.layout.selecte_image_view, parent, false)
            return ViewHolder(v)
        }
    }

    inner class  ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageView: ImageView = itemView.findViewById(R.id.target_image)
        var checkBox :ImageView = itemView.findViewById(R.id.checkbox)
    }

    override fun onSelect(position: Int) {
        if(selected.contains(address[position])){
            selected.remove(address[position])
        }else {
            selected.add(address[position])
        }

    }
}