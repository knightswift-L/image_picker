package com.example.image_picker.page

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.util.component1
import androidx.core.util.component2
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.HORIZONTAL
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.canhub.cropper.CropImageView
import com.example.image_picker.R
import com.example.image_picker.adapter.ViewHolder
import com.example.image_picker.util.SelectImageUtil
import java.io.File

class CropImageActivity : AppCompatActivity() {
    private lateinit var cropImageView:CropImageView
    private lateinit var cancelCropBtn:TextView
    private lateinit var confirmCroBtn:TextView
    private lateinit var recyclerView: RecyclerView
    private var aspectRatioX: Int = 0
    private var aspectRatioY: Int = 0
    private var images:ArrayList<String> = arrayListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crop_image)
         initView()

    }

    private fun initView(){
        images = intent.getStringArrayListExtra("uri") ?: arrayListOf<String>()
        aspectRatioX = intent.getIntExtra("aspectRatioX",0)
        aspectRatioY = intent.getIntExtra("aspectRatioY",0)
        cropImageView = findViewById(R.id.cropImageView)
        recyclerView = findViewById(R.id.preViewRecycler)
        recyclerView.layoutManager = LinearLayoutManager(this).apply {
            orientation = HORIZONTAL
        }
        recyclerView.adapter = LinearAdapter(images)
        if(aspectRatioX != 0 && aspectRatioY != 0) {
            cropImageView.setAspectRatio(aspectRatioX, aspectRatioY)
        }
        cropImageView.isAutoZoomEnabled = true
        cropImageView.setMultiTouchEnabled(false)
        cancelCropBtn = findViewById(R.id.cancel_crop_image)
        confirmCroBtn = findViewById(R.id.confirm_crop_image)
        cancelCropBtn.setOnClickListener {
        finish()
        }

        cropImageView.setOnCropImageCompleteListener { view, result ->
            if(result.isSuccessful){
                val data = Intent()
                data.putStringArrayListExtra("Image", arrayListOf(result.uriContent.toString()))
                setResult(RESULT_OK,data)
                finish()
            }
        }

        confirmCroBtn.setOnClickListener {
            val uri = SelectImageUtil().createImageUri(this.applicationContext)
            cropImageView.croppedImageAsync(customOutputUri = uri, saveCompressQuality = 100)
        }
        if(images.isNotEmpty()){
        cropImageView.setImageUriAsync(Uri.parse(images.first()))
        }
    }

    inner class LinearAdapter(private val images:ArrayList<String>) :RecyclerView.Adapter<ViewHolder>(){
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v: View = LayoutInflater.from(parent.context).inflate(R.layout.crop_image_preview_item, parent, false)
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            var type = ImageView.ScaleType.CENTER_CROP
            val params = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            params.width = 200
            params.height = 200
            params.gravity = Gravity.CENTER
            holder.imageView.scaleType = type
            holder.container.layoutParams = params

            Glide.with(applicationContext).load(Uri.parse(images[position])).apply(
                 RequestOptions.
                 fitCenterTransform())
                .into(holder.imageView);
        }

        override fun getItemCount(): Int {
           return images.size
        }


    }
    inner class  ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageView: ImageView = itemView.findViewById(R.id.crop_image_item)
        var container : ConstraintLayout = itemView.findViewById(R.id.image_container)
    }
}