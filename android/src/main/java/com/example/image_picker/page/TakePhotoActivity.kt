package com.example.image_picker.page

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import androidx.lifecycle.LifecycleOwner
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.image_picker.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class TakePhotoActivity : AppCompatActivity(),CameraXConfig.Provider {
    private lateinit var cameraProvider : ProcessCameraProvider
    private lateinit var previewView:PreviewView
    private lateinit var thumbnail:ImageView
    private lateinit var switchCamera:ImageView
    private lateinit var btnTakePhoto: Button
    private lateinit var imageCapture:ImageCapture
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var outputDirectory: File
    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK
    companion object{
        private const val TAG = "CameraXBasic"
        private const val FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val PHOTO_EXTENSION = ".jpg"
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0

        /** Helper function used to create a timestamped file */
        private fun createFile(baseFolder: File, format: String, extension: String) =
            File(baseFolder, SimpleDateFormat(format, Locale.US)
                .format(System.currentTimeMillis()) + extension)

        /** Use external media if it is available, our app's file directory otherwise */
        fun getOutputDirectory(context: Context): File {
            val appContext = context.applicationContext
            val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
                File(it,"image_picker").apply { mkdirs() } }
            return if (mediaDir != null && mediaDir.exists())
                mediaDir else appContext.filesDir
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_take_photo)
         val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        previewView = findViewById(R.id.previewView)
        btnTakePhoto = findViewById(R.id.take_photo)
        thumbnail = findViewById(R.id.thumbtail)
        switchCamera = findViewById(R.id.switch_camera)
        btnTakePhoto.isEnabled = false
        cameraExecutor = Executors.newSingleThreadExecutor()
        outputDirectory = getOutputDirectory(this)
        previewView.post {
            cameraProviderFuture.addListener(Runnable {
                cameraProvider = cameraProviderFuture.get()
                lensFacing = when {
                    hasBackCamera() -> CameraSelector.LENS_FACING_BACK
                    hasFrontCamera() -> CameraSelector.LENS_FACING_FRONT
                    else -> throw IllegalStateException("Back and front camera are unavailable")
                }
                updateCameraSwitchButton()
                bindPreview(cameraProvider)
            }, ContextCompat.getMainExecutor(this))
        }
        btnTakePhoto.setOnClickListener{
            captureImage()
        }
        switchCamera.let{
            it.isEnabled = false
            it.setOnClickListener{
            lensFacing = if(lensFacing == CameraSelector.LENS_FACING_BACK){
                CameraSelector.LENS_FACING_FRONT
            }else{
                CameraSelector.LENS_FACING_BACK
            }
                bindPreview(cameraProvider)
            }
        }
        thumbnail.setOnClickListener {
            val intent = Intent(this,SelectImageActivity::class.java)
            startActivity(intent)
        }

    }

    private fun updateCameraSwitchButton() {
        try {
            switchCamera.isEnabled = hasBackCamera() && hasFrontCamera()
        } catch (exception: CameraInfoUnavailableException) {
            switchCamera.isEnabled = false
        }
    }

    private fun bindPreview(cameraProvider : ProcessCameraProvider) {
        var height: Int?
        var width: Int?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
           val size = windowManager.currentWindowMetrics.bounds
           height = size.height()
           width = size.width()
        } else {
            val resources: Resources = this.resources
            val metrics = resources.displayMetrics
            height = metrics.heightPixels
            width = metrics.widthPixels
        }

        val targetAspectRation = aspectRatio(width,height)
        val rotation = previewView.display.rotation
        var preview : Preview = Preview.Builder()
            .setTargetAspectRatio(targetAspectRation)
            .setTargetRotation(rotation)
            .build()

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setTargetAspectRatio(targetAspectRation)
            .setTargetRotation(rotation)
            .build()
        var cameraSelector : CameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()

        preview.setSurfaceProvider(previewView.surfaceProvider)
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(this as LifecycleOwner, cameraSelector, preview,imageCapture)
        btnTakePhoto.isEnabled = true
    }

    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    private fun hasBackCamera(): Boolean {
        return cameraProvider?.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) ?: false
    }
    private fun hasFrontCamera(): Boolean {
        return cameraProvider?.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) ?: false
    }

    override fun getCameraXConfig(): CameraXConfig {
        return CameraXConfig.Builder.fromConfig(Camera2Config.defaultConfig())
            .setAvailableCamerasLimiter(CameraSelector.DEFAULT_BACK_CAMERA)
            .build()
    }

    private fun captureImage(){
        imageCapture.let { imageCapture ->
            val photoFile = createFile(outputDirectory, FILENAME, PHOTO_EXTENSION)
            val metadata = ImageCapture.Metadata().apply {
                isReversedHorizontal = lensFacing == CameraSelector.LENS_FACING_FRONT
            }
            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile)
                .setMetadata(metadata)
                .build()

            imageCapture.takePicture(
                outputOptions, cameraExecutor, object : ImageCapture.OnImageSavedCallback {
                    override fun onError(exc: ImageCaptureException) {
                        Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                    }

                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        val savedUri = output.savedUri ?: Uri.fromFile(photoFile)
                        Log.d(TAG, "Photo capture succeeded: $savedUri")

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            runOnUiThread {
                                Glide.with(applicationContext)
                                    .load(savedUri)
                                    .apply(RequestOptions.circleCropTransform())
                                    .into(thumbnail)
                            }
                        }

                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                            sendBroadcast(
                                Intent(android.hardware.Camera.ACTION_NEW_PICTURE, savedUri)
                            )
                        }
                        val mimeType = MimeTypeMap.getSingleton()
                            .getMimeTypeFromExtension(savedUri.toFile().extension)
                        MediaScannerConnection.scanFile(
                            applicationContext,
                            arrayOf(savedUri.toFile().absolutePath),
                            arrayOf(mimeType)
                        ) { _, uri ->
                            Log.d(TAG, "Image capture scanned into media store: $uri")
                        }
                        runOnUiThread {
                            val data = Intent()
                            data.putExtra("Image", savedUri.toString())
                            setResult(RESULT_OK, data)
                            finish()
                        }
                    }
                })

        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when(keyCode){
            KeyEvent.KEYCODE_VOLUME_DOWN ->{
                captureImage()
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }

}