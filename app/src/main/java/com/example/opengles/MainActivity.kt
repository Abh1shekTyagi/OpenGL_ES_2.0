package com.example.opengles

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.opengl.GLSurfaceView
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.MotionEvent
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.opengles.databinding.ActivityMainBinding
import kotlin.math.atan2

enum class Operation(val symbol: String) {
    PLUS("+"), MINUS("-"), DIVIDE("/"), MULTIPLY("*"), NONE("")
}

class MainActivity : AppCompatActivity() {
    private var vibrator: Vibrator? = null
    private var animator: ObjectAnimator? = null
    private lateinit var binding: ActivityMainBinding
    private lateinit var renderer: MyRenderer
    private var previousX: Float = 0f
    private var previousY: Float = 0f
    private var height = 0
    private var width = 0
    private var result: Long? = null
    private var number: Long? = null
    private var operation = Operation.NONE

    private fun setupUI() {
        height = binding.surfaceView.height
        width = binding.surfaceView.width
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        renderer = MyRenderer(binding.root.context)
        vibrator = binding.root.context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
//        renderer.setModel(inputStream)
        setupUI()
        initUI()
        initListener()

        binding.surfaceView.setOnTouchListener { _, e ->
            val x: Float = e.x
            val y: Float = e.y
            when (e.action) {
                MotionEvent.ACTION_MOVE -> {
                    val dx: Float = x - width / 2
                    val dy: Float = y
                    renderer.setAngleY(
                        (Math.toDegrees(atan2(dy, dx).toDouble()).toFloat() - 67) * -1
                    )
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

    private fun initUI() {
        binding.surfaceView.setEGLContextClientVersion(2) //app will crash if this is not initialised to use GLS 2.0
        binding.surfaceView.setRenderer(renderer)//this is how to set the renderer
        // Render the view only when there is a change in the drawing data.
        // To allow the triangle to rotate automatically, this line is commented out:
        //runs only when requestRender() is called
        binding.surfaceView.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
    }

    private fun initListener() {
        binding.zero.setOnClickListener {
            if (number != 0L) append(0)
            else {
                gentleVibration()
            }
        }
        binding.one.setOnClickListener {
            append(1)
        }
        binding.two.setOnClickListener {
            append(2)
        }
        binding.three.setOnClickListener {
            append(3)
        }
        binding.four.setOnClickListener {
            append(4)
        }
        binding.five.setOnClickListener {
            append(5)
        }
        binding.six.setOnClickListener {
            append(6)
        }
        binding.seven.setOnClickListener {
            append(7)
        }
        binding.eight.setOnClickListener {
            append(8)
        }
        binding.nine.setOnClickListener {
            append(9)
        }

        binding.reset.setOnClickListener {
//            if (number == null && result == null) {
//                errorAnimation()
//                return@setOnClickListener
//            }
            gentleVibration()
            number = null
            result = null
            binding.userInput.text = ""
            renderer.reset()
            binding.surfaceView.requestRender()
            binding.errorText.visibility = View.INVISIBLE
        }

        binding.back.setOnClickListener {
            gentleVibration()
            number = number?.div(10L)
            renderer.remove()
            binding.surfaceView.requestRender()
            binding.userInput.text =
                resources.getString(R.string.inputString, number.toString(), "", "")
        }

        binding.divide.setOnClickListener {
            try {
                gentleVibration()
                val old = result
                result = number?.let { it1 -> result?.div(it1) } ?: number
                outputResult(old)
                operation = Operation.DIVIDE
                binding.userInput.text =
                    resources.getString(
                        R.string.inputString,
                        result.toString(),
                        operation.symbol,
                        ""
                    )
            } catch (e: Exception) {
                number = null
                result = null
                renderer.error(false)
                binding.errorText.visibility = View.VISIBLE
                binding.errorText.text = resources.getString(R.string.why_would_you_do_that)
                binding.surfaceView.requestRender()
            }

        }

        binding.plus.setOnClickListener {
            try {
                gentleVibration()
                val old = result
                result = number?.let { it1 -> result?.plus(it1) } ?: number
                outputResult(old)
                operation = Operation.PLUS
                binding.userInput.text =
                    resources.getString(
                        R.string.inputString,
                        result.toString(),
                        operation.symbol,
                        ""
                    )
            } catch (e: Exception) {
                number = null
                result = null
                renderer.error(false)
                binding.errorText.visibility = View.VISIBLE
                binding.errorText.text = resources.getString(R.string.why_would_you_do_that)
                binding.surfaceView.requestRender()
            }
        }

        binding.multiply.setOnClickListener {
            try {
                gentleVibration()
                val old = result
                result = number?.let { it1 -> result?.times(it1) } ?: number
                outputResult(old)
                operation = Operation.MULTIPLY
                binding.userInput.text =
                    resources.getString(
                        R.string.inputString,
                        result.toString(),
                        operation.symbol,
                        ""
                    )

            } catch (e: Exception) {
                number = null
                result = null
                renderer.error(false)
                binding.errorText.visibility = View.VISIBLE
                binding.errorText.text = resources.getString(R.string.please_uninstall_me)
                binding.surfaceView.requestRender()
            }
        }

        binding.equals.setOnClickListener {
            binding.userInput.text =
                resources.getString(
                    R.string.inputString,
                    result.toString(),
                    operation.symbol,
                    if (operation == Operation.NONE) "" else number.toString()
                )
            when (operation) {
                Operation.PLUS -> {
                    if (number == 0L) errorAnimation()
                    val oldResult = result
                    try {
                        result = number?.let { it1 -> result?.plus(it1) }
                        outputResult(oldResult)
                    } catch (exception: Exception) {
                        number = null
                        result = null
                        renderer.error(false)
                        binding.errorText.visibility = View.VISIBLE
                        binding.surfaceView.requestRender()
                    }

                }

                Operation.DIVIDE -> {
                    if (number == 0L) errorAnimation()
                    val oldResult = result
                    try {
                        result = number?.let { it1 -> result?.div(it1) }
                        outputResult(oldResult)
                    } catch (exception: Exception) {
                        number = 0
                        result = 0
                        renderer.error(false)
                        binding.errorText.visibility = View.VISIBLE
                        binding.errorText.text =
                            resources.getString(R.string.have_you_ever_been_to_school)
                        binding.surfaceView.requestRender()
                    }
                }

                Operation.MINUS -> {
                    if (number == 0L) errorAnimation()
                    val oldResult = result
                    try {
                        result = number?.let { it1 -> result?.minus(it1) }
                        outputResult(oldResult)
                    } catch (exception: Exception) {
                        number = 0
                        result = 0
                        renderer.error(false)
                        binding.errorText.visibility = View.VISIBLE
                        binding.surfaceView.requestRender()
                    }

                }

                Operation.MULTIPLY -> {
                    if (number == 0L) errorAnimation()
                    val oldResult = result
                    try {
                        result = number?.let { it1 -> result?.times(it1) }
                        outputResult(oldResult)
                    } catch (exception: Exception) {
                        number = 0
                        result = 0
                        renderer.error(false)
                        binding.errorText.text = resources.getString(R.string.system_hil_gya)
                        binding.errorText.visibility = View.VISIBLE
                        binding.surfaceView.requestRender()
                    }

                }

                Operation.NONE -> {
                    if (number == result) errorAnimation()
                    else {
                        result = number
                    }
                }
            }

            operation = Operation.NONE

        }
        binding.calculatorParent.setOnClickListener { }
    }

    private fun append(i: Int) {
        binding.errorText.visibility = View.INVISIBLE
        renderer.error(true)
        (number?.toDouble()?.times(10)?.plus(i))?.let {
            if (it.toString().toFloat() >= Long.MAX_VALUE) {
                errorAnimation()
                return
            }
        }
        if (number == result) number = 0
        number = number?.times(10)?.plus(i)
        try {
            number?.let { renderer.drawNumber(it) }
            binding.surfaceView.requestRender()
            binding.userInput.text = if (operation == Operation.NONE)
                resources.getString(R.string.inputString, number.toString(), "", "") else {
                resources.getString(
                    R.string.inputString,
                    result.toString(),
                    operation.symbol,
                    number.toString()
                )
            }
            gentleVibration()
        } catch (e: Exception) {
            number = 0
            result = 0
        }
    }

    private fun outputResult(oldResult: Long?) {
        binding.userInput.text = resources.getString(
            R.string.result,
            oldResult.toString(),
            operation.symbol,
            number.toString(),
            result.toString()
        )
        result?.let { renderer.drawNumber(it) }
        number = result
        binding.surfaceView.requestRender()
        gentleVibration()
    }

    private fun gentleVibration() {
        vibrator?.cancel()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Gentle vibration pattern: [0ms delay, 150ms vibration, 50ms pause, 150ms vibration]
            val vibrationPattern = longArrayOf(0, 100, 100)
            vibrator?.vibrate(
                VibrationEffect.createWaveform(
                    vibrationPattern,
                    -1
                )
            ) // -1 means no repeat
        } else {
            // For older devices (API < 26)
            val vibrationPattern = longArrayOf(0, 100, 100)
            vibrator?.vibrate(vibrationPattern, -1) // -1 means no repeat
        }
    }

    private fun popSound() {

    }

    private fun errorAnimation() {
        // Animate the view - Shake effect
        animator?.cancel()
        animator = ObjectAnimator.ofFloat(
            binding.calculatorParent,
            "translationX",
            0f,
            25f,
            -25f,
            25f,
            -25f,
            0f
        )
        animator?.duration = 500 // duration in milliseconds
        animator?.interpolator = OvershootInterpolator()
        animator?.start()

        // Trigger device vibration
        vibrator?.cancel()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // For devices running Android 8.0 (API 26) and above
            vibrator?.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            // For older devices
            vibrator?.vibrate(500)
        }
    }
}