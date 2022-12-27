package com.example.image_picker.page
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.image_picker.PreImageView
import com.example.image_picker.R

class PreViewActivity : AppCompatActivity(){
    private lateinit var preView:PreImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pre_view)
        preView = findViewById(R.id.pre_image_view)
        val uri = intent.getStringExtra("Image")
        uri?.let {
            preView.uri = Uri.parse(it)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}