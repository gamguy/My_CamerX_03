package com.example.mycamerx03

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Matrix
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.util.Rational
import android.util.Size
import android.view.Surface
import android.view.TextureView
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

private const val REQUEST_CODE_PERMISSIONS = 10
private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

val permissions = arrayOf(android.Manifest.permission.CAMERA,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE)


class MainActivity : AppCompatActivity() {

    private val filename = "test.png"
    private val sd = Environment.getExternalStorageDirectory()
    private val dest = File(sd, filename)
    private var lensFacing = CameraX.LensFacing.BACK
    private var imageCapture: ImageCapture? = null
    private lateinit var viewFinder: TextureView

//    private var fab_camera? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewFinder = findViewById(R.id.view_finder)
        bindCamera()
//        var fab_camera = findViewById(R.id.fab_camera)

//        val fab_camera = findViewById<FloatingActionButton>(R.id.fab_camera)
        fab_camera.setOnClickListener {
            imageCapture?.takePicture(dest,
                object : ImageCapture.OnImageSavedListener {
                    override fun onError(error: ImageCapture.UseCaseError,
                                         message: String, exc: Throwable?) {
                        Log.e("Image", error.toString())
                    }
                    override fun onImageSaved(file: File) {
                        Log.v("Image", "Successfully saved image")
                    }
                })
        }


//        val fab_flash = findViewById<FloatingActionButton>(R.id.fab_flash)
        fab_flash.setOnClickListener {
            val flashMode = imageCapture?.flashMode
            if(flashMode == FlashMode.ON) imageCapture?.flashMode = FlashMode.OFF
            else imageCapture?.flashMode = FlashMode.ON
        }

//        val fab_switch_camera = findViewById<FloatingActionButton>(R.id.fab_switch_camera)
        fab_switch_camera.setOnClickListener {
            lensFacing = if (CameraX.LensFacing.FRONT == lensFacing) {
                CameraX.LensFacing.BACK
            } else {
                CameraX.LensFacing.FRONT
            }
            bindCamera()
        }
    }

//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//        if (requestCode == REQUEST_CODE_PERMISSIONS) {
//            if (allPermissionsGranted()) {
//                view_finder.post { startCamera() }
//            } else {
//                Toast.makeText(this,
//                        "Permissions not granted by the user.",
//                        Toast.LENGTH_SHORT).show()
//                finish()
//            }
//        }
//    }

//    private fun startCamera() {
//        // Create configuration object for the viewfinder use case
//        val previewConfig = PreviewConfig.Builder().apply {
//            setTargetAspectRatio(Rational(1, 1))
//            setTargetResolution(Size(640, 640))
//        }.build()
//
//        // Build the viewfinder use case
//        val preview = Preview(previewConfig)
//
//        // Every time the viewfinder is updated, recompute layout
//        preview.setOnPreviewOutputUpdateListener {
//
//            // To update the SurfaceTexture, we have to remove it and re-add it
//            val parent = view_finder.parent as ViewGroup
//            parent.removeView(viewFinder)
//            parent.addView(viewFinder, 0)
//
//            view_finder.surfaceTexture = it.surfaceTexture
//            updateTransform()
//        }
//
//        // Bind use cases to lifecycle
//        // If Android Studio complains about "this" being not a LifecycleOwner
//        // try rebuilding the project or updating the appcompat dependency to
//        // version 1.1.0 or higher.
//        CameraX.bindToLifecycle(this, preview)
//    }
////    At this point, we need to implement the mysterious `updateTransform()` method.
////    Inside of `updateTransform()` the goal is to compensate for changes in device orientation
////    to display our viewfinder in upright rotation:
//
//    private fun updateTransform() {
//        val matrix = Matrix()
//
//        // Compute the center of the view finder
//        val centerX = viewFinder.width / 2f
//        val centerY = viewFinder.height / 2f
//
//        // Correct preview output to account for display rotation
//        val rotationDegrees = when(viewFinder.display.rotation) {
//            Surface.ROTATION_0 -> 0
//            Surface.ROTATION_90 -> 90
//            Surface.ROTATION_180 -> 180
//            Surface.ROTATION_270 -> 270
//            else -> return
//        }
//        matrix.postRotate(-rotationDegrees.toFloat(), centerX, centerY)
//
//        // Finally, apply transformations to our TextureView
//        viewFinder.setTransform(matrix)
//    }


//    private fun allPermissionsGranted() =
//            REQUIRED_PERMISSIONS.all {
//                ContextCompat.checkSelfPermission(
//                        baseContext, it) == PackageManager.PERMISSION_GRANTED
//            }
//    }

    private fun bindCamera() {
        CameraX.unbindAll()

        // Preview config for the camera
        val previewConfig = PreviewConfig.Builder()
            .setLensFacing(lensFacing)
            .build()

        val preview = Preview(previewConfig)
        // The view that displays the preview
        val textureView: TextureView = findViewById(R.id.view_finder)
        // Handles the output data of the camera
        preview.setOnPreviewOutputUpdateListener { previewOutput ->
//            val parent = textureView.parent as ViewGroup
//            parent.removeView(textureView)
            // Displays the camera image in our preview view
            textureView.surfaceTexture = previewOutput.surfaceTexture
        }
        // Image capture config which controls the Flash and Lens
        val imageCaptureConfig = ImageCaptureConfig.Builder()
            .setTargetRotation(windowManager.defaultDisplay.rotation)
            .setLensFacing(lensFacing)
            .setFlashMode(FlashMode.ON)
            .build()


        imageCapture = ImageCapture(imageCaptureConfig)
        CameraX.bindToLifecycle(this, imageCapture,preview)
//
//        // Bind the camera to the lifecycle
//        CameraX.bindToLifecycle(this as LifecycleOwner, imageCapture, preview)
    }

    override fun onStart() {
        super.onStart()

        if (hasNoPermissions()) {
            requestPermission()
        }
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(this, permissions,0)
    }

    private fun hasNoPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(this,
            Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
            Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
    }
}