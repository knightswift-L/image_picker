package com.example.image_picker.page

import android.content.ContentUris
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
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
    lateinit var btn_cancel: TextView
    lateinit var btn_confirm: TextView
    lateinit var adapter: GridAdapter
    var selected = mutableListOf<Image>()
    var address = arrayListOf<Image>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_image)
        initView()
        initRecyclerView()
        queryMediaData()
        Log.i("TEST","getMediaData End")
    }

    private fun initView(){
        recyclerView = findViewById(R.id.recyclerView)
        btn_cancel = findViewById<Button>(R.id.cancel_select_image_button)
        btn_confirm = findViewById<Button>(R.id.confirm_select_image_button)
        btn_cancel.setOnClickListener{
            finish()
        }
        btn_confirm.setOnClickListener{
            val data = Intent()
            var temp = mutableListOf<String>()
            temp.addAll(selected.map {it.uri })
            data.putStringArrayListExtra("Image", temp as ArrayList<String>)
            setResult(RESULT_OK, data)
            finish()
        }
    }

    private fun initRecyclerView(){
        recyclerView.layoutManager = GridLayoutManager(this,3)
        val width = getWindowWidth()
        val imageWidth = width /3
        adapter = GridAdapter(this,selected,imageWidth)
        recyclerView.adapter = adapter
    }


    private fun queryMediaData() {
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
                   address.addAll(videoList)
                   adapter.notifyDataSetChanged()
               }
           }
            Log.i("TEST","getMediaData Function End")
        }

    }

    private fun getWindowWidth():Int{
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val size = windowManager.currentWindowMetrics.bounds
            size.width()
        } else {
            val resources: Resources = this.resources
            val metrics = resources.displayMetrics
            metrics.widthPixels
        }
    }

    inner class GridAdapter(var onChange: OnSelectedChange, temp:List<Image>, val itemWidth:Int): RecyclerView.Adapter<ViewHolder>() {
        private var selected:MutableList<Image> = temp.toMutableList()

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            var type = ImageView.ScaleType.CENTER_CROP
            val params = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            params.width = itemWidth
            params.height = itemWidth
            params.gravity = Gravity.CENTER
            holder.imageView.scaleType = type
            holder.container.layoutParams = params
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
                RequestOptions.fitCenterTransform())
                .into(holder.imageView);
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
        var container : ConstraintLayout = itemView.findViewById(R.id.image_container)
    }

    override fun onSelect(position: Int) {
        if(selected.contains(address[position])){
            selected.remove(address[position])
        }else {
            selected.add(address[position])
        }

    }
}