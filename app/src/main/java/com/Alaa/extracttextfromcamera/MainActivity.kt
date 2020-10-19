package com.Alaa.extracttextfromcamera

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.SparseArray
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.text.TextBlock
import com.google.android.gms.vision.text.TextRecognizer
import java.io.IOException

class MainActivity : AppCompatActivity() {
    lateinit var cameraView : SurfaceView
    lateinit var textView : TextView
    lateinit var cameraSource : CameraSource
    var RequestCameraPermissionID:Int =1001
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cameraView = findViewById(R.id.surface_view) as SurfaceView
        textView = findViewById(R.id.text_view) as TextView
        val textRecognizer = TextRecognizer.Builder(getApplicationContext()).build()
        if (!textRecognizer.isOperational()) {
            Log.w("MainActivity", "Detector dependencies are not yet available")
        } else {
            cameraSource = CameraSource.Builder(getApplicationContext(), textRecognizer)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(1280, 1024)
                .setRequestedFps(2.0f)
                .setAutoFocusEnabled(true)
                .build()
            cameraView.getHolder().addCallback(object : SurfaceHolder.Callback {
                override fun surfaceCreated(p0: SurfaceHolder) {
                    try {
                        if (ActivityCompat.checkSelfPermission(
                                getApplicationContext(),
                                Manifest.permission.CAMERA
                            ) !== PackageManager.PERMISSION_GRANTED
                        ) {
                            ActivityCompat.requestPermissions(
                                this@MainActivity,
                                arrayOf<String>(Manifest.permission.CAMERA),
                                RequestCameraPermissionID
                            )
                            return
                        }
                        cameraSource.start(cameraView.getHolder())
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }

                override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {

                }

                override fun surfaceDestroyed(p0: SurfaceHolder) {
                    cameraSource.stop()
                }

            })

            textRecognizer.setProcessor(object : Detector.Processor<TextBlock> {
                override fun release() {
                }

                override fun receiveDetections(p0: Detector.Detections<TextBlock>?) {
                    var items: SparseArray<TextBlock> = p0!!.getDetectedItems()
                    textView.post(object : Runnable {
                        public override fun run() {
                            val stringBuilder = StringBuilder()
                            for (i in 0 until items.size()) {
                                val item = items.valueAt(i)
                                stringBuilder.append(item.getValue())
                                stringBuilder.append("\n")
                            }
                            textView.setText(stringBuilder.toString())
                        }
                    })
                }
            })
        }
    }
    override fun onRequestPermissionsResult(requestCode:Int, @NonNull permissions:Array<String>, @NonNull grantResults:IntArray) {
        when (requestCode) {
            RequestCameraPermissionID -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) !== PackageManager.PERMISSION_GRANTED)
                    {
                        return
                    }
                    try
                    {
                        cameraSource.start(cameraView.getHolder())
                    }
                    catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}