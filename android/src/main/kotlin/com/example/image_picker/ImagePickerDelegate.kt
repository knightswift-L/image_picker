package com.example.image_picker

import android.Manifest
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.image_picker.util.SelectImageUtil
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.PluginRegistry.ActivityResultListener
import io.flutter.plugin.common.PluginRegistry.RequestPermissionsResultListener


class ImagePickerDelegate(private val activity:Activity) : ActivityResultListener,
    RequestPermissionsResultListener {
    private var pendingResult: MethodChannel.Result? = null
    private var methodCall: MethodCall? = null
    private var tempImage:String? = null;
    companion object{
         const val SELECT_IMAGE_CODE = 10000000
         const val REQUEST_IMAGE_CAPTURE = 10000001
        const val REQUEST_PERMISSION = 10000001
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
        val intent = Intent(activity,SelectImageActivity::class.java)
        activity.startActivityForResult(intent,SELECT_IMAGE_CODE)
    }

    fun takePhoto(result: MethodChannel.Result, methodCall: MethodCall){
        if(!setPendingMethodCallAndResult(methodCall,result)){
            finishWithError("0","Busing","Busing")
            return
        }
        checkPermission();
    }
    private fun takePhoto(){
        var file = SelectImageUtil().createImageFile(activity.applicationContext)
        tempImage = file.absolutePath
        Log.e("Test",file.absolutePath)
        Log.e("Test","${activity.applicationContext.packageName}.fileProvider")
        val photoURI: Uri = FileProvider.getUriForFile(
            activity.applicationContext,
            "${activity.applicationContext.packageName}.fileProvider",
            file
        )
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
        try {
            activity.startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } catch (e: ActivityNotFoundException) {

        }
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
            if(resultCode == RESULT_OK) {
                var result = data?.getStringArrayListExtra("Image");
                var targets = mutableListOf<String>()
                result?.forEach{
                    var path = SelectImageUtil().writeToCacheDir(activity.applicationContext, Uri.parse(it))
                    if (path != null){
                        targets.add(path)
                    }
                }
                Log.i("Test",targets.joinToString(","))
                finishWithSuccess(targets)
                return true
            }
        }else if(requestCode == REQUEST_IMAGE_CAPTURE){
            Log.e("Test","${resultCode == RESULT_OK}")
            if(resultCode == RESULT_OK) {
                finishWithSuccess(if(tempImage == null) arrayListOf() else arrayListOf(tempImage))
                tempImage = null
                return true
            }else{
                Log.e("Test",data?.data.toString())
            }
        }
        finishWithError("0","sjdlasjdljsk","daskd;skdl;")
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
                finishWithError("not Permission","not Permission","not Permission")
            }
        }
        return true
    }

    private fun finishWithSuccess(data:Any?){
        pendingResult!!.success(data)
        clear()
    }
    private fun finishWithError(errorCode: String, errorMessage: String, error: Any?){
        pendingResult!!.error(errorCode,errorMessage,error)
        clear()
    }

    private fun clear(){
        pendingResult = null;
        methodCall = null;
    }
}