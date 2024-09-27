package com.example.opengles

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

//this is an interface that we have to implement and then set it to GLSurfaceView
//by GLSurfaceView.setRenderer()

//Note: Opengl coordinate system assumes square shape geometry, but it is not always true so
//To solve this problem, you can apply OpenGL projection modes and camera views to transform coordinates
// so your graphic objects have the correct proportions on any display.
//camera view and projection view are defined by matrices


//OpenGl pipeline
//vertex shader -> shape assembly -> resterization -> fragment shader -> testing and blending -> frame buffer -> user screen


//Shape faces and winding
//by default: counterclockwise front and clockwise is the back side, this can be reversed but not advised.

//Texture compression
//it can significantly increase the performance by reducing memory requirements and
// making more efficient use of memory bandwidth

//Android Extension Pack (AEP)
//can be used to maintain consistency on all devices.
//If your app requires OpenGL ES 3.2 you do not need to require the AEP.

//To use it define in manifest like this
//<uses-feature android:name="android.hardware.opengles.aep"
//              android:required="true" />

//to check if it supports
//var deviceSupportsAEP: Boolean =
//        packageManager.hasSystemFeature(PackageManager.FEATURE_OPENGLES_EXTENSION_PACK)

//take a look at TextureView as well

private const val EGL_CONTEXT_CLIENT_VERSION = 0x3098
private const val glVersion = 3.0
const val TOUCH_SCALE_FACTOR: Float = 180.0f / 320f

//private class ContextFactory : GLSurfaceView.EGLContextFactory {
//    companion object{
//        val TAG: String = Companion::class.java.name
//
//    }
//    override fun createContext(
//        egl: EGL10?,
//        display: javax.microedition.khronos.egl.EGLDisplay?,
//        eglConfig: EGLConfig?
//    ): javax.microedition.khronos.egl.EGLContext? {
//        Log.w(TAG, "creating OpenGL ES $glVersion context")
//        return egl?.eglCreateContext(
//            display,
//            eglConfig,
//            EGL10.EGL_NO_CONTEXT,
//            intArrayOf(EGL_CONTEXT_CLIENT_VERSION, glVersion.toInt(), EGL10.EGL_NONE)
//        ) // returns null if 3.0 is not supported    }
//    }
//
//    override fun destroyContext(
//        egl: EGL10?,
//        display: javax.microedition.khronos.egl.EGLDisplay?,
//        context: javax.microedition.khronos.egl.EGLContext?
//    ) {
//    }
//}
class ObjectRender: GLSurfaceView.Renderer {


    private lateinit var mTriangle: Triangle
    private lateinit var mSquare: Square

    // vPMatrix is an abbreviation for "Model View Projection Matrix"
    private val vPMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val rotationMatrix = FloatArray(16)

    @Volatile
    var angle: Float = 0f

    //this method is called only once so use it for
    //setting OpenGL environment parameters or initializing OpenGL graphic objects
    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        mTriangle = Triangle()
        mSquare = Square()
    }

    //This method is called on each redraw of the GLSurfaceView
    //This should be used for drawing (and re-drawing) graphic objects
    override fun onDrawFrame(unused: GL10) {
        val scratch = FloatArray(16)

        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        // Set the camera position (View matrix)
        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 3f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)

        // Calculate the projection and view transformation
        Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

        // Create a rotation transformation for the triangle
//        val time = SystemClock.uptimeMillis() % 4000L
//        val angle = 0.090f * time.toInt()
        Matrix.setRotateM(rotationMatrix, 0, angle, 0f, 0f, -1.0f)

        // Combine the rotation matrix with the projection and camera view
        // Note that the vPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.
        Matrix.multiplyMM(scratch, 0, vPMatrix, 0, rotationMatrix, 0)

        // Draw triangle
        mTriangle.draw(scratch)

        // Draw shape
//        mTriangle.draw(vPMatrix)
    }

    //This is called when the geometry changes like size change or orientation change/ config changes
    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)

        val ratio: Float = width.toFloat() / height.toFloat()

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 7f)
    }
}