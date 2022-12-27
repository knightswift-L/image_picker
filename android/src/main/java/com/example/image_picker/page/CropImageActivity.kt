package com.example.image_picker.page

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.media.Image
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.canhub.cropper.CropImageView
import com.example.image_picker.R
import com.example.image_picker.model.GlobalLanguage
import com.example.image_picker.util.SelectImageUtil

data class CropImageFile(val originPath:String,var cropImagePath:String?)

class CropImageActivity : AppCompatActivity() {
    private lateinit var cropImageView:CropImageView
    private lateinit var cancelCropBtn:TextView
    private lateinit var confirmCroBtn:TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LinearAdapter
    private var aspectRatioX: Int = 0
    private var aspectRatioY: Int = 0
    private var images:ArrayList<CropImageFile> = arrayListOf<CropImageFile>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crop_image)
        initData()
        initView()

    }
    private fun initData(){
        var imageData = intent.getStringArrayListExtra("uri") ?: arrayListOf<String>()
        imageData.forEach{
            images.add(CropImageFile(originPath = it, cropImagePath = null))
        }
        aspectRatioX = intent.getIntExtra("aspectRatioX",0)
        aspectRatioY = intent.getIntExtra("aspectRatioY",0)
    }

    private fun initView(){
        cropImageView = findViewById(R.id.cropImageView)
        recyclerView = findViewById(R.id.preViewRecycler)
        recyclerView.layoutManager = LinearLayoutManager(this).apply {
            orientation = HORIZONTAL
        }
        adapter = LinearAdapter(this.applicationContext,images,object :LinearAdapter.OnClickListener{
            override fun OnClick(position: Int) {
               cropImageView.resetCropRect()
                cropImageView.setImageUriAsync(Uri.parse(if( images[position].cropImagePath == null) images[position].originPath else images[position].cropImagePath))
            }
        })
        recyclerView.adapter = adapter
        if(aspectRatioX != 0 && aspectRatioY != 0) {
            cropImageView.setAspectRatio(aspectRatioX, aspectRatioY)
        }
        cropImageView.isAutoZoomEnabled = true
        cropImageView.setMultiTouchEnabled(true)
        cancelCropBtn = findViewById(R.id.cancel_crop_image)
        cancelCropBtn.text = GlobalLanguage.getCurrentLanguage().getCancel()
        confirmCroBtn = findViewById(R.id.confirm_crop_image)
        confirmCroBtn.text = GlobalLanguage.getCurrentLanguage().getConfirm()
        cancelCropBtn.setOnClickListener {
        finish()
        }

        cropImageView.setOnCropImageCompleteListener { view, result ->
            if(result.isSuccessful){
             if(images.size == 1){
                 val data = Intent()
                 data.putStringArrayListExtra("Image", arrayListOf(result.uriContent.toString()))
                 setResult(RESULT_OK,data)
                 finish()
             }else{
                 view.setImageUriAsync(result.uriContent)
                 for (it in images.indices){
                     if(images[it].originPath == result.originalUri?.toString()){
                         images[it].cropImagePath = result.uriContent?.toString()
                         adapter.notifyItemChanged(it)
                         break
                     }
                 }

             }
            }
        }

        confirmCroBtn.setOnClickListener {
            val uri = SelectImageUtil().createImageUri(this.applicationContext)
            cropImageView.croppedImageAsync(customOutputUri = uri, saveCompressQuality = 100)
        }
        if(images.isNotEmpty()){
            cropImageView.setImageUriAsync(Uri.parse(images.first().originPath))
        }
    }



}
 class  ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var imageView: ImageView = itemView.findViewById(R.id.crop_image_item)
     var alreadyImage:ImageView = itemView.findViewById(R.id.already_image)
}

class LinearAdapter(private val context:Context,private val images:ArrayList<CropImageFile>,val onClick:OnClickListener) :RecyclerView.Adapter<ViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v: View = LayoutInflater.from(parent.context).inflate(R.layout.crop_image_preview_item, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.imageView.setOnClickListener {
            onClick.OnClick(position)
        }
        holder.alreadyImage.visibility = if(images[position].cropImagePath != null) VISIBLE else INVISIBLE
        Glide.with(context).load(Uri.parse(if(images[position].cropImagePath == null) images[position].originPath else images[position].cropImagePath)).apply(
            RequestOptions.
            fitCenterTransform())
            .into(holder.imageView);
    }

    override fun getItemCount(): Int {
        return images.size
    }

    interface OnClickListener{
        fun OnClick(position: Int)
    }
}