package com.devdroid.videoexample2

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.support.v4.os.IResultReceiver2.Default
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.MediaController
import android.widget.VideoView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


class MainActivity : AppCompatActivity(){
    private var videoLayout: LinearLayout? = null
    private var videoViewCount = 0
    private lateinit var sensorManager: SensorManager
    private lateinit var rotationVectorSensor: Sensor
    private val sensorDelay = SensorManager.SENSOR_DELAY_NORMAL
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.main)
        ) { v: View, insets: WindowInsetsCompat ->
            val systemBars =
                insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)!!
        if (rotationVectorSensor == null) {} else {
            sensorManager.registerListener(gyroscopeSensorListener, rotationVectorSensor, sensorDelay)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
            && !Environment.isExternalStorageManager()
        ) {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.setData(Uri.parse("package:" + applicationContext.packageName))
            startActivity(intent)
        } else videoLayout = findViewById(R.id.videoLayout)
        //VideoView videoView = findViewById(R.id.videoView);
        videoLayout?.let { addVideoView(it)
        }
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    private fun addVideoView(layout: LinearLayout) {
        val videoView = VideoView(this)
        val layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        videoView.layoutParams = layoutParams
        val count = videoViewCount.toString()
        val vPath = "/storage/emulated/0/Download/$videoViewCount.mp4"
        val videoURI = Uri.parse(vPath)
        videoView.setVideoURI(videoURI)
        videoView.start()

        val mediaController = MediaController(this)
        mediaController.setAnchorView(videoView)
        videoView.setMediaController(mediaController)

        // Добавление кнопки для перехода в полноэкранный режим
        val fullScreenButton = Button(this)
        fullScreenButton.text = "Full Screen"
        fullScreenButton.setBackgroundColor(Color.BLACK)
        val fullScreenParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        fullScreenButton.layoutParams = fullScreenParams
        fullScreenButton.setOnClickListener {
            val intent = Intent(this, VideoActivity::class.java)
            intent.putExtra("videoUri", videoURI.toString())
            startActivity(intent)
        }

        layout.addView(videoView)
        layout.addView(fullScreenButton)
        videoViewCount++
    }
    private val gyroscopeSensorListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                val rotationMatrix = FloatArray(9)
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)

                val orientationValues = FloatArray(3)
                SensorManager.getOrientation(rotationMatrix, orientationValues)

                val azimuth = Math.toDegrees(orientationValues[0].toDouble()).toFloat()
                val pitch = Math.toDegrees(orientationValues[1].toDouble()).toFloat()
                val roll = Math.toDegrees(orientationValues[2].toDouble()).toFloat()

                updateBackgroundDisplay(azimuth, pitch, roll)
            }}}

    private fun launchVlcRtspStream(rtspUrl: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
            && !Environment.isExternalStorageManager()
        ) {
            val package_name = "org.videolan.vlc"
            val intent_uri =
                "rtsp:"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(intent_uri))
            intent.setPackage(package_name)
            startActivity(intent)
        }
    }

    private fun updateBackgroundDisplay(azimuth: Float, pitch: Float, roll: Float) {
        videoLayout?.gravity = Gravity.CENTER
        videoLayout?.rotation=-roll


    }

    fun addVideoViewOnClick(view: View?) {
        videoLayout?.let { addVideoView(it) }
    }

    val isExternalStorageReadable: Boolean
        get() {
            val state = Environment.getExternalStorageState()
            return if (Environment.MEDIA_MOUNTED == state || Environment.MEDIA_MOUNTED_READ_ONLY == state) {
                true
            } else false
        }
}


