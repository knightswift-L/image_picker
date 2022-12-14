package com.example.image_picker

import android.Manifest
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.image_picker.model.CFile
import com.example.image_picker.model.GlobalLanguage
import com.example.image_picker.page.CropImageActivity
import com.example.image_picker.page.SelectImageActivity
import com.example.image_picker.page.TakePhotoActivity
import com.example.image_picker.util.SelectImageUtil
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.PluginRegistry.ActivityResultListener
import io.flutter.plugin.common.PluginRegistry.RequestPermissionsResultListener


class ImagePickerDelegate(private val activity:Activity) : ActivityResultListener, RequestPermissionsResultListener {
    private var pendingResult: MethodChannel.Result? = null
    private var methodCall: MethodCall? = null
    private var maxQuality:Int = 1
    private var aspectRatioX:Int? = null
    private var aspectRatioY:Int? = null
    companion object{
        const val SELECT_IMAGE_CODE = 10000000
        const val REQUEST_IMAGE_CAPTURE = 10000001
        const val REQUEST_PERMISSION = 10000002
        const val CROP_IMAGE_CODE = 10000003
    }

    private fun setPendingMethodCallAndResult(
        methodCall: MethodCall, result: MethodChannel.Result
    ): Boolean {
        if (pendingResult != null) {
            return false
        }
        this.methodCall = methodCall
        pendingResult = result
        return true
    }

    fun selectImage(result: MethodChannel.Result, methodCall: MethodCall) {
        if(!setPendingMethodCallAndResult(methodCall,result)){
            finishWithError("0","Busing","Busing")
            return
        }
        val arguments:Map<String,String> =  methodCall.arguments as Map<String,String>
        if(arguments["maxQuality"] != null){
            maxQuality = arguments["maxQuality"]!!.toInt()
        }

        if(arguments["cropOption"] != null){
            val cropOption:Map<String,String> =  arguments["cropOption"] as Map<String,String>
            aspectRatioX = cropOption["aspectRatioX"]!!.toInt()
            aspectRatioY = cropOption["aspectRatioY"]!!.toInt()
        }
        val intent = Intent(activity, SelectImageActivity::class.java)
        activity.startActivityForResult(intent,SELECT_IMAGE_CODE)
    }

    fun takePhoto(result: MethodChannel.Result, methodCall: MethodCall){
        if(!setPendingMethodCallAndResult(methodCall,result)){
            finishWithError("0","Busing","Busing")
            return
        }
        val arguments:Map<String,String> =  methodCall.arguments as Map<String,String>
        if(arguments["maxQuality"] != null){
            maxQuality = arguments["maxQuality"]!!.toInt()
        }

        if(arguments["language"] != null){
            GlobalLanguage.switch(arguments["language"] ?:"")
        }

        if(arguments["cropOption"] != null){
            val cropOption:Map<String,String> =  arguments["cropOption"] as Map<String,String>
            Log.e("TEST",cropOption.values.toString())
            aspectRatioX = cropOption["aspectRatioX"]!!.toInt()
            aspectRatioY = cropOption["aspectRatioY"]!!.toInt()

            Log.e("TEST","$aspectRatioX")
            Log.e("TEST","$aspectRatioY")
        }
        checkPermission();
    }
    private fun takePhoto(){
        val intent = Intent(activity, TakePhotoActivity::class.java)
        activity.startActivityForResult(intent,REQUEST_IMAGE_CAPTURE)
    ///使用系统相机拍照
//        val uri = SelectImageUtil().createImageUri(activity.applicationContext)
//        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
//        try {
//            activity.startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
//        } catch (e: ActivityNotFoundException) {
//        }
    }

    private fun checkPermission(){
        when {
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                takePhoto()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.CAMERA
            ) -> {
               false
            }
            else -> {
                activity.requestPermissions(arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),REQUEST_PERMISSION)
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if(requestCode == SELECT_IMAGE_CODE){
            if(resultCode == RESULT_OK && data != null) {
                var result = data.getStringArrayListExtra("Image") ?: arrayListOf()
                Log.e("Test",result.size.toString() ?: "Empty")
                if(aspectRatioX != null && aspectRatioY != null){
                    val intent = Intent(activity.applicationContext, CropImageActivity::class.java)
                    intent.putStringArrayListExtra("uri", result)
                    intent.putExtra("aspectRatioX",aspectRatioX)
                    intent.putExtra("aspectRatioY",aspectRatioY)
                    activity.startActivityForResult(intent,CROP_IMAGE_CODE)
                    return true
                }else {
                    var targets = mutableListOf<CFile>()
                    result.forEach {
                        var path = SelectImageUtil().writeToCacheDir(
                            activity.applicationContext,
                            Uri.parse(it)
                        )
                        if (path != null) {
                            targets.add(CFile(height = 100, width = 100, size = 0, path = path))
                        }
                    }
                    finishWithSuccess(targets)
                }
                return true
            }
            finishWithSuccess(mutableListOf<CFile>())
            return true
        }else if(requestCode == REQUEST_IMAGE_CAPTURE){
            if(resultCode == RESULT_OK && data != null) {
                var result = data.getStringExtra("Image")
                if(result != null) {
                    return if(aspectRatioX != null && aspectRatioY != null){
                        val intent = Intent(activity.applicationContext, CropImageActivity::class.java)
                        intent.putStringArrayListExtra("uri", arrayListOf(result.toString()))
                        intent.putExtra("aspectRatioX",aspectRatioX)
                        intent.putExtra("aspectRatioY",aspectRatioY)
                        activity.startActivityForResult(intent,CROP_IMAGE_CODE)
                        true
                    }else{
                        var targetImage = SelectImageUtil().writeToCacheDir(
                            activity.applicationContext,
                            Uri.parse(result)
                        )
                        finishWithSuccess(listOf(CFile(height = 100, width = 100, size = 0, path = targetImage!!)))
                        true
                    }
                }else{
                    finishWithSuccess(listOf<CFile>())
                }
                return true
            }else{
                Log.e("Test",data?.data.toString())
            }
        }else if(requestCode == CROP_IMAGE_CODE){
            if(resultCode == RESULT_OK && data != null) {
                var result = data.getStringArrayListExtra("Image")
                var targetImages = mutableListOf<CFile>()
                result?.forEach {
                    var targetImage = SelectImageUtil().writeToCacheDir(
                        activity.applicationContext,
                        Uri.parse(it)
                    )
                    targetImages.add( CFile(height = 100, width = 100, size = 0, path = targetImage!!))
                }
                    finishWithSuccess(
                        targetImages
                    )
                return true
            }

        }
        finishWithSuccess(listOf<CFile>())
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ): Boolean {
        if(requestCode == REQUEST_PERMISSION){
            if (permissions.size == grantResults.size && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                takePhoto()
            }else{
                finishWithError("1","not Permission","not Permission")
            }
        }
        return true
    }

    private fun finishWithSuccess(data:List<CFile>){
        pendingResult!!.success(data.map { it.toMap() }.toList())
        clear()
    }
    private fun finishWithError(errorCode: String, errorMessage: String, error: Any?){
        pendingResult!!.error(errorCode,errorMessage,error)
        clear()
    }

    private fun clear(){
        pendingResult = null
        methodCall = null
    }
}