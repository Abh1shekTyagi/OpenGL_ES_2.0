package com.example.opengles

import android.content.Context
import android.opengl.GLES32
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import com.example.opengles.numbersModel.Eight
import com.example.opengles.numbersModel.Five
import com.example.opengles.numbersModel.Four
import com.example.opengles.numbersModel.Nine
import com.example.opengles.numbersModel.One
import com.example.opengles.numbersModel.Seven
import com.example.opengles.numbersModel.Six
import com.example.opengles.numbersModel.Three
import com.example.opengles.numbersModel.Two
import com.example.opengles.numbersModel.Zero
import com.example.opengles.shapes.ArbitraryShape
import com.example.opengles.shapes.CharacterA
import com.example.opengles.shapes.CharacterS
import com.example.opengles.shapes.Cube
import com.example.opengles.shapes.HalfCone
import com.example.opengles.shapes.PhongSphere
import com.example.opengles.shapes.Pyramid
import com.example.opengles.shapes.Sphere
import java.io.InputStream
import javax.microedition.khronos.opengles.GL10


class MyRenderer(val contextParam: Context) : GLSurfaceView.Renderer {
    private var byPass: Boolean = true
    private lateinit var inputStream: InputStream
    private val mModelMatrix =
        FloatArray(16) //model  matrix, we do scaling, rotation, shear, and transformation on this matrix
    private val mRotationZ =
        FloatArray(16) //model  matrix, we do scaling, rotation, shear, and transformation on this matrix
    private val mRotationX =
        FloatArray(16) //model  matrix, we do scaling, rotation, shear, and transformation on this matrix
    private val mRotationY =
        FloatArray(16) //model  matrix, we do scaling, rotation, shear, and transformation on this matrix
    private val mViewMatrix =
        FloatArray(16) //view matrix, Positions and orients the camera within the world.
    private val mMVMatrix =
        FloatArray(16) //model view matrix(combines the model and view), Simplifies shader calculations by pre-combining model and view transformations

    private var angleX = 0f
    private var angleY = 0f
    fun setAngleX(angleParam: Float) {
        angleX = angleParam
    }

    fun setAngleY(angleParam: Float) {
        angleY = angleParam * 2.5f
    }

    private val mProjectionMatrix =
        FloatArray(16) //projection matrix, Defines how 3D objects are projected onto the 2D screen.

    private val mMVPMatrix =
        FloatArray(16) //model view projection matrix,Final transformation matrix used in vertex shaders to position vertices on the screen.
    private var mtriangle: Pyramid? = null
    private var mCube: Cube? = null
    private var mA: CharacterA? = null
    private var one: One? = null
    private var mCone: HalfCone? = null
    private var mArbitraryShape: ArbitraryShape? = null
    private var mCharS: CharacterS? = null
    private var mSphere: Sphere? = null
    private var phongSphere: PhongSphere? = null
    private val drawNumber = Array<Any>(10) {}
    private var number = 0
    private val numberList = mutableListOf<Int>()


    override fun onSurfaceCreated(gl: GL10?, config: javax.microedition.khronos.egl.EGLConfig?) {
        // Set the background frame color to black
        GLES32.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
//        mtriangle = Pyramid()
//        mCube = Cube()
        var start = System.currentTimeMillis()
        drawNumber[0] = Zero(contextParam)
        Log.d("timeanaysic", "zero -> ${System.currentTimeMillis() - start}")
        start = System.currentTimeMillis()
        drawNumber[1] = One(contextParam)
        Log.d("timeanaysic", "one -> ${System.currentTimeMillis() - start}")
        start = System.currentTimeMillis()
        drawNumber[2] = Two(contextParam)
        Log.d("timeanaysic", "two -> ${System.currentTimeMillis() - start}")
        start = System.currentTimeMillis()
        drawNumber[3] = Three(contextParam)
        Log.d("timeanaysic", "three -> ${System.currentTimeMillis() - start}")
        start = System.currentTimeMillis()
        drawNumber[4] = Four(contextParam)
        Log.d("timeanaysic", "four -> ${System.currentTimeMillis() - start}")
        start = System.currentTimeMillis()
        drawNumber[5] = Five(contextParam)
        Log.d("timeanaysic", "five -> ${System.currentTimeMillis() - start}")
        start = System.currentTimeMillis()
        drawNumber[6] = Six(contextParam)
        Log.d("timeanaysic", "six -> ${System.currentTimeMillis() - start}")
        start = System.currentTimeMillis()
        drawNumber[7] = Seven(contextParam)
        Log.d("timeanaysic", "seven -> ${System.currentTimeMillis() - start}")
        start = System.currentTimeMillis()
        drawNumber[8] = Eight(contextParam)
        Log.d("timeanaysic", "eight -> ${System.currentTimeMillis() - start}")
        start = System.currentTimeMillis()
        drawNumber[9] = Nine(contextParam)
        Log.d("timeanaysic", "nine -> ${System.currentTimeMillis() - start}")
        start = System.currentTimeMillis()

//        mCone = HalfCone()
//        mArbitraryShape = ArbitraryShape()
//        mCharS = CharacterS()
//        mSphere = Sphere()
//        phongSphere = PhongSphere()
    }

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        // Adjust the view based on view window changes, such as screen rotation
        GLES32.glViewport(0, 0, width, height)// 0,0 is lower left, and (w,h) top right.
        val ratio = width.toFloat() / height
        val left = -ratio
        val right = ratio
        Matrix.frustumM(mProjectionMatrix, 0, left, right, -1.0f, 1.0f, 1f, 100f)
    }

    override fun onDrawFrame(unused: GL10) {
        // Draw background color
        GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT or GLES32.GL_DEPTH_BUFFER_BIT)
        GLES32.glClearDepthf(1.0f) //set up the depth buffer
        GLES32.glEnable(GLES32.GL_DEPTH_TEST) //enable depth test (so, it will not look through the surfaces)
        GLES32.glDepthFunc(GLES32.GL_LEQUAL) //indicate what type of depth test
        // Set the camera position (View matrix)
        Matrix.setLookAtM(
            mViewMatrix, 0,
            0.0f, 0f, 1.0f,  //camera is at (0,0,1)
            0f, 0f, 0f,  //looks at the origin
            0f, 1f, 0.0f
        ) //head is down (set to (0,1,0) to look from the top)

        if (!byPass) return
        if (numberList.size == 0) numberList.add(0)//it will make sure 0 is always visible
        else if (numberList.size > 1 && numberList[0] == 0) numberList.removeFirst()
        for ((i, number) in numberList.withIndex()) { //we will get concurrent modification exception
            Matrix.setIdentityM(
                mMVPMatrix,
                0
            ) //set the model view projection matrix to an identity matrix
            Matrix.setIdentityM(mMVMatrix, 0) //set the model view  matrix to an identity matrix
            Matrix.setIdentityM(mModelMatrix, 0) //set the model matrix to an identity matrix
            Matrix.setIdentityM(mRotationY, 0)
            Matrix.setIdentityM(mRotationZ, 0)
            Matrix.setIdentityM(mRotationX, 0)

            Matrix.scaleM(
                mModelMatrix,
                0,
                0.09f / (numberList.size),
                0.09f / (numberList.size),
                0.09f / (numberList.size)
            ) //move backward for 5 units


            if (numberList.size % 2 == 0) {
                Matrix.translateM(
                    mModelMatrix,
                    0,
                    if (i < numberList.size / 2) {
                        (i + 1) - (numberList.size / 2) - 0.5f
                    } else {
                        i - numberList.size / 2 + 0.5f
                    } * 9f,
                    (numberList.size - 2).toFloat(),
                    -5f
                ) //move backward for 5 units
            } else {
                Matrix.translateM(
                    mModelMatrix,
                    0,
                    (i - numberList.size / 2) * 9f,
                    (numberList.size - 2).toFloat(),
                    -5f
                ) //move backward for 5 units
            }

            Matrix.rotateM(mRotationX, 0, -90f, 1f, 0f, 0.0f)
            Matrix.rotateM(mRotationZ, 0, angleY, 0f, 0f, 1.0f)
//            Matrix.rotateM(mRotationY, 0, angleX, 0f, 1f, 0.0f)
            Matrix.multiplyMM(mModelMatrix, 0, mModelMatrix, 0, mRotationX, 0)
            Matrix.multiplyMM(mModelMatrix, 0, mModelMatrix, 0, mRotationY, 0)
            Matrix.multiplyMM(mModelMatrix, 0, mModelMatrix, 0, mRotationZ, 0)
            // Calculate the projection and view transformation
            //calculate the model view matrix
            Matrix.multiplyMM(mMVMatrix, 0, mViewMatrix, 0, mModelMatrix, 0) //AB is not equal BA
            Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVMatrix, 0)

//        mtriangle?.draw(mMVPMatrix)
//        mCube?.draw(mMVPMatrix)
//        mA?.draw(mMVPMatrix)
//        mArbitraryShape?.draw(mMVPMatrix)
//        val temp = mMVPMatrix.copyOf()
//
//        one?.draw(mMVPMatrix)
//        mCharS?.draw(mMVPMatrix)
//        mCone?.draw(mMVPMatrix)
//        mSphere?.draw(mMVPMatrix)
//        phongSphere?.draw(mMVPMatrix)
            when (val obj = drawNumber[number]) {
                is Zero -> obj.draw(mMVPMatrix)
                is One -> obj.draw(mMVPMatrix)
                is Two -> obj.draw(mMVPMatrix)
                is Three -> obj.draw(mMVPMatrix)
                is Four -> obj.draw(mMVPMatrix)
                is Five -> obj.draw(mMVPMatrix)
                is Six -> obj.draw(mMVPMatrix)
                is Seven -> obj.draw(mMVPMatrix)
                is Eight -> obj.draw(mMVPMatrix)
                is Nine -> obj.draw(mMVPMatrix)
            }
        }
    }

    fun remove() {
        if (numberList.isNotEmpty())
            numberList.removeLast()
        angleY = 0f
    }

    fun drawNumber(numberParam: Long) {
        numberList.clear()
        numberList.addAll(numberParam.toString().map { it.toString().toInt() })
        angleY = 0f
    }

    fun reset() {
        numberList.clear()
        angleY = 0f
        byPass = true
    }

    fun error(param: Boolean) {
        byPass = param
    }

    companion object {
        fun checkGlError(glOperation: String) {
            var error: Int
            if ((GLES32.glGetError().also { error = it }) != GLES32.GL_NO_ERROR) {
                Log.e("MyRenderer", "$glOperation: glError $error")
            }
        }

        fun loadShader(type: Int, shaderCode: String?): Int {
            // create a vertex shader  (GLES32.GL_VERTEX_SHADER) or a fragment shader (GLES32.GL_FRAGMENT_SHADER)
            val shader = GLES32.glCreateShader(type)
            GLES32.glShaderSource(
                shader,
                shaderCode
            ) // add the source code to the shader and compile it
            GLES32.glCompileShader(shader)
            return shader
        }
    }
}