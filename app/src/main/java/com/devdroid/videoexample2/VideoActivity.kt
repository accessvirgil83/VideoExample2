package com.devdroid.videoexample2

import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Button
import android.widget.SeekBar
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity

class VideoActivity : AppCompatActivity() {
    private lateinit var videoView: VideoView
    private lateinit var seekBar: SeekBar
    private var isPlaying = true
    private var touchX: Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)

        videoView = findViewById(R.id.fullscreenVideoView)
        seekBar = findViewById(R.id.seekBar)

        val videoUriString = intent.getStringExtra("videoUri")
        if (!videoUriString.isNullOrEmpty()) {
            val videoUri = Uri.parse(videoUriString)
            videoView.setVideoURI(videoUri)
            videoView.start()
        }

        seekBar.max = videoView.duration
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    videoView.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        videoView.setOnPreparedListener {
            seekBar.max = videoView.duration
        }
        val closeFullScreenButton: Button = findViewById(R.id.closefullscreenButton)
        closeFullScreenButton.setOnClickListener {
            finish() // Закрываем активность для возврата к предыдущей
        }
        videoView.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (isPlaying) {
                        videoView.pause()
                    } else {
                        videoView.start()
                    }
                    isPlaying = !isPlaying
                    touchX = event.x
                    return@setOnTouchListener true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = event.x - touchX
                    videoView.seekTo(videoView.currentPosition + deltaX.toInt() * 1000)
                    touchX = event.x
                    return@setOnTouchListener true
                }
            }
            return@setOnTouchListener false
        }
    }
}
