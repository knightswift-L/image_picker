package com.example.image_picker

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import java.io.BufferedInputStream

class PreImageView(context: Context,attributeSet: AttributeSet?) : FrameLayout(context,attributeSet) {
    private var scaleGestureDetector:ScaleGestureDetector
    private lateinit var gestureDetector:GestureDetector
    private var preView:ImageView
    private var displayWidth:Int = 0
    private var displayHeight:Int = 0
    private var originWidth:Int = 0
    private var originHeight:Int = 0
    private val floatPoints = FloatArray(8)
    private val scope:CoroutineScope
    private var scaleFactor:Float = 1F
    private var imageMatrix:Matrix = Matrix()
    var uri:Uri? = null
    set(value) {
        field = value
        displayHeight = context.resources.displayMetrics.heightPixels
        displayWidth = context.resources.displayMetrics.widthPixels
        initImage(value!!)
    }

    init {
        val inflater = LayoutInflater.from(context)
        val v = inflater.inflate(R.layout.preview_image, this, true)
        preView = v.findViewById(R.id.ImageView_image)
        preView.scaleType = ImageView.ScaleType.MATRIX
        scope = CoroutineScope(Job() + Dispatchers.IO)
        displayHeight = context.resources.displayMetrics.heightPixels
        displayWidth = context.resources.displayMetrics.widthPixels
        gestureDetector = GestureDetector(context,object : GestureDetector.OnGestureListener{
            override fun onDown(p0: MotionEvent): Boolean {
                return true
            }

            override fun onShowPress(p0: MotionEvent) {

            }

            override fun onSingleTapUp(p0: MotionEvent): Boolean {
                return false
            }

            override fun onScroll(p0: MotionEvent, p1: MotionEvent, p2: Float, p3: Float): Boolean {
                if(Math.abs(p2) > 20 ||Math.abs(p3) > 20 ) {
                    imageMatrix.postTranslate(-p2, -p3)
                    preView.imageMatrix = imageMatrix
                    return true
                }
                return false
            }

            override fun onLongPress(p0: MotionEvent) {
            }

            override fun onFling(p0: MotionEvent, p1: MotionEvent, p2: Float, p3: Float): Boolean {
                return false
            }
        })
        scaleGestureDetector = ScaleGestureDetector(context,object :
        ScaleGestureDetector.OnScaleGestureListener{
            override fun onScale(p0: ScaleGestureDetector): Boolean {
                var temp = scaleFactor * p0.scaleFactor
                if(temp in 0.1F..3.0F) {
                    scaleFactor = temp
                    scaleImage(p0.scaleFactor)
                }
                return true
            }

            override fun onScaleBegin(p0: ScaleGestureDetector): Boolean {
                return true
            }

            override fun onScaleEnd(p0: ScaleGestureDetector) {

            }
        })
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            return gestureDetector.onTouchEvent(event) || scaleGestureDetector.onTouchEvent(event) || super.onTouchEvent(event)
        }
        return false
    }

    private fun initImage(uri: Uri){
        try {
            context.contentResolver.openInputStream(uri).use {
                val buff = BufferedInputStream(it)
                var bytes = buff.readBytes()
                var bitmap =  BitmapFactory.decodeByteArray(bytes,0,bytes.size)
                originWidth = bitmap.width
                originHeight = bitmap.height
                floatPoints[0] = 0F
                floatPoints[1] = 0F
                floatPoints[2] = originWidth.toFloat()
                floatPoints[3] = 0F
                floatPoints[4] = originWidth.toFloat()
                floatPoints[5] = originHeight.toFloat()
                floatPoints[6] = 0F
                floatPoints[7] = originHeight.toFloat()
                imageMatrix.mapPoints(floatPoints)
                val widthScaleFactor = displayWidth / originWidth.toFloat()
                val heightScaleFactor = displayHeight / originHeight.toFloat()
                if(widthScaleFactor >=1.0 && heightScaleFactor >=1.0){
                    scaleFactor  = Math.min(widthScaleFactor,heightScaleFactor)
                }else if(widthScaleFactor < 1.0 && heightScaleFactor < 1.0){
                    scaleFactor = Math.max(widthScaleFactor,heightScaleFactor)
                }else{
                    scaleFactor = Math.min(widthScaleFactor,heightScaleFactor)
                }
                scaleImage(scaleFactor)
                preView.setImageBitmap(bitmap)
            }
        }catch (e:Exception){

        }
    }

    fun scaleImage(scale:Float){
        imageMatrix.reset()
        imageMatrix.postScale(scaleFactor,scaleFactor)
        imageMatrix.postTranslate((displayWidth - originWidth * scaleFactor)/2F,(displayHeight - originHeight * scaleFactor)/2F)
        preView.imageMatrix = imageMatrix
    }
}