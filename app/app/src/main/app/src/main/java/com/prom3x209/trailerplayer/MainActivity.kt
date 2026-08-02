package com.prom3x209.trailerplayer

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.VideoView

class MainActivity : Activity() {

    private var nextComponent: String? = null
    private var handedOff = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        } else {
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            )
        }

        val path = intent.getStringExtra("video_path")
        nextComponent = intent.getStringExtra("next_component")

        if (path.isNullOrEmpty()) {
            handOffAndFinish()
            return
        }

        val videoView = VideoView(this)
        videoView.setVideoURI(Uri.fromFile(java.io.File(path)))

        videoView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                handOffAndFinish()
            }
            true
        }
        videoView.setOnCompletionListener { handOffAndFinish() }
        videoView.setOnErrorListener { _, _, _ -> handOffAndFinish(); true }

        val root = FrameLayout(this)
        root.addView(
            videoView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )
        setContentView(root)
        videoView.start()
    }

    private fun handOffAndFinish() {
        if (handedOff) return
        handedOff = true

        val target = nextComponent
        if (!target.isNullOrEmpty() && target.contains("/")) {
            val slash = target.indexOf("/")
            val pkg = target.substring(0, slash)
            var activity = target.substring(slash + 1)
            if (activity.startsWith("."))
                activity = pkg + activity

            try {
                val i = Intent()
                i.setClassName(pkg, activity)
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(i)
            } catch (e: Exception) {
            }
        }
        finish()
    }
}
