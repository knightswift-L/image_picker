package com.example.image_picker.util

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class SelectImageUtil {
    fun writeToCacheDir(context: Context,uri: Uri):String?{
        var targetPath = getCacheFileName(context)
        var file: File? = null
        try {
            file = File(targetPath)
            file.createNewFile()
            val fos = FileOutputStream(file)
            val resolver = context.contentResolver
            resolver.openInputStream(uri).use { stream ->
               var bytes = stream?.readBytes()
                if(bytes != null){
                    fos.write(bytes)
                    fos.flush()
                    fos.close()
                }else{
                    return null;
                }
            }
        } catch (e: Exception) {
            return null
        }
        return file.absolutePath
    }

    fun writeToCacheDir(context: Context, bitmap: Bitmap):String?{
        var targetPath = getCacheFileName(context)
        var file: File? = null
        try {
            file = File(targetPath)
            file.createNewFile()
            val bos = ByteArrayOutputStream()
            Log.e("Test",bitmap.width.toString())
            Log.e("Test",bitmap.height.toString())
            bitmap.compress(Bitmap.CompressFormat.JPEG, 0, bos)
            val bitmapdata = bos.toByteArray()
            val fos = FileOutputStream(file)
            fos.write(bitmapdata)
            fos.flush()
            fos.close()
        } catch (e: Exception) {
            return null;
        }
        return file.absolutePath
    }

    private fun getCacheFileName(context: Context):String{
        var dic = context.filesDir
       return "${dic!!.absolutePath}/${Date().time}.jpeg"
    }

    fun createImageFile(context: Context): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir = context.filesDir
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        )
    }
}