package com.example.image_picker.page

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.hardware.Camera.ACTION_NEW_PICTURE
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
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.LifecycleOwner
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.image_picker.R
import com.google.common.util.concurrent.ListenableFuture
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
    private lateinit var btn_take_photo: Button
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
        btn_take_photo = findViewById(R.id.take_photo)
        thumbnail = findViewById(R.id.thumbtail)
        btn_take_photo.isEnabled = false
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
                bindPreview(cameraProvider)
            }, ContextCompat.getMainExecutor(this))
        }
        btn_take_photo.setOnClickListener{
            captureImage()
        }
    }

    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
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
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        preview.setSurfaceProvider(previewView.getSurfaceProvider())

        var camera = cameraProvider.bindToLifecycle(this as LifecycleOwner, cameraSelector, preview,imageCapture)
        btn_take_photo.isEnabled = true
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

            // Create output file to hold the image
            val photoFile = createFile(outputDirectory, FILENAME, PHOTO_EXTENSION)

            // Setup image capture metadata
            val metadata = ImageCapture.Metadata().apply {

                // Mirror image when using the front camera
                isReversedHorizontal = lensFacing == CameraSelector.LENS_FACING_FRONT
            }

            // Create output options object which contains file + metadata
            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile)
                .setMetadata(metadata)
                .build()

            // Setup image capture listener which is triggered after photo has been taken
            imageCapture.takePicture(
                outputOptions, cameraExecutor, object : ImageCapture.OnImageSavedCallback {
                    override fun onError(exc: ImageCaptureException) {
                        Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                    }

                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        val savedUri = output.savedUri ?: Uri.fromFile(photoFile)
                        Log.d(TAG, "Photo capture succeeded: $savedUri")

                        // We can only change the foreground Drawable using API level 23+ API
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            // Update the gallery thumbnail with latest picture taken
//                                setGalleryThumbnail(savedUri)
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

            // We can only change the foreground Drawable using API level 23+ API
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                // Display flash animation to indicate that photo was captured
//                    fragmentCameraBinding.root.postDelayed({
//                        fragmentCameraBinding.root.foreground = ColorDrawable(Color.WHITE)
//                        fragmentCameraBinding.root.postDelayed(
//                            { fragmentCameraBinding.root.foreground = null }, ANIMATION_FAST_MILLIS)
//                    }, ANIMATION_SLOW_MILLIS)
            }
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