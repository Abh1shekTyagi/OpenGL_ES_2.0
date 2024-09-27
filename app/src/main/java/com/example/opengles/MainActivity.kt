package com.example.opengles

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.opengles.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var renderer: ObjectRender
    private var previousX: Float = 0f
    private var previousY: Float = 0f


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        renderer = ObjectRender()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.surfaceView.setEGLContextClientVersion(2) //app will crash if this is not initialised to use GLS 2.0
        binding.surfaceView.setRenderer(renderer)//this is how to set the renderer
        // Render the view only when there is a change in the drawing data.
        // To allow the triangle to rotate automatically, this line is commented out:
        //runs only when requestRender() is called
//         binding.surfaceView.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY

        binding.surfaceView.setOnTouchListener { _, e ->
            val x: Float = e.x
            val y: Float = e.y
            when (e.action) {
                MotionEvent.ACTION_MOVE -> {
                    var dx: Float = x - previousX
                    var dy: Float = y - previousY

                    // reverse direction of rotation above the mid-line
                    if (y > binding.root.height / 2) {
                        dx *= -1
                    }

                    // reverse direction of rotation to left of the mid-line
                    if (x < binding.root.width / 2) {
                        dy *= -1
                    }

                    renderer.angle += (dx + dy) * TOUCH_SCALE_FACTOR
                    binding.surfaceView.requestRender()
                }

                else -> {
                    false
                }

            }
            previousX = x
            previousY = y
            true
        }
    }
}