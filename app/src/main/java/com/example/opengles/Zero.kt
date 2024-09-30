package com.example.opengles

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLES32
import com.example.opengles.Utils.Companion.loadShader
import com.example.opengles.objloader.ObjLoader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer

class Zero(val context: Context) {
    private val mProgram: Int
    private val mPositionHandle: Int
    private val mMVPMatrixHandle: Int
    private var mPointLightLocationHandle = 0
    private val mColorHandle: Int
    private var lightLocation = floatArrayOf(1f, 1f, 0f) //x,y,z

    private val vertexShaderCode = "attribute vec3 aVertexPosition;" +  //vertex of an object
            " attribute vec4 aVertexColor;" +  //the colour  of the object
            "     uniform mat4 uMVPMatrix;" +  //model view  projection matrix
            "     uniform vec3 uPointLightingLocation;" +  //model view  projection matrix
            "    varying float vPointLightWeighting;" +  //variable to be accessed by the fragment shader
            "    varying vec4 vColor;" +  //variable to be accessed by the fragment shader
            "void main() {" +
            "        vec4  mvPosition = uMVPMatrix* vec4(aVertexPosition, 1.0);" +  //the position of the vertex
            "        float dist_from_light = distance(uPointLightingLocation , mvPosition.xyz);" + //distance from the light source
            "        vPointLightWeighting = 10.0/(dist_from_light * dist_from_light);" +  //intensity for the light source
            "gl_Position = uMVPMatrix* vec4(aVertexPosition, 1.0);" +  //calculate the position of the vertex
            "        vColor=aVertexColor;}" //get the colour from the application program

    private val fragmentShaderCode =
        "precision lowp float;" +  //need to set to low in order to show the depth map
                "varying vec4 vColor;" +  //variable from the vertex shader
                "    varying float vPointLightWeighting;" +  //point light intensity
                "void main() {" +
                "float depth=1.0-gl_FragCoord.z;" +  //to show closer surface to be brighter, and further away surface darker
                "gl_FragColor = vec4(vColor.xyz*vPointLightWeighting,1) ;" +
                "}"

    // initialize vertex byte buffer for shape coordinates
    var cubeBuffer: FloatBuffer = ByteBuffer.allocateDirect(charAVertex.size * 4).run {
        // use the device hardware's native byte order
        order(ByteOrder.nativeOrder())
        // create a floating point buffer from the ByteBuffer
        asFloatBuffer().apply {
            // add the coordinates to the FloatBuffer
            put(charAVertex)
            // set the buffer to read the first coordinate
            position(0)
        }
    }    // initialize vertex byte buffer for shape coordinates

    private var indexBuffer: IntBuffer =
        IntBuffer.allocate(charAIndices.size).apply {
            put(charAIndices.toIntArray())
            position(0)
        }

    private var cubeColorBuffer: FloatBuffer =
        ByteBuffer.allocateDirect(charAColor.size * 4).run {
            // use the device hardware's native byte order
            order(ByteOrder.nativeOrder())
            // create a floating point buffer from the ByteBuffer
            asFloatBuffer().apply {
                // add the coordinates to the FloatBuffer
                put(charAColor)
                // set the buffer to read the first coordinate
                position(0)
            }
        }

    private val vertexStride: Int = COORDS_PER_VERTEX * 4 // 4 bytes per vertex
    private val colorStride: Int = COLOUR_PER_VERTEX * 4

    init {
        // prepare shaders and OpenGL program
        val vertexShader: Int = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader: Int = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        // create empty OpenGL ES Program
        mProgram = GLES32.glCreateProgram().also {

            // add the vertex shader to program
            GLES20.glAttachShader(it, vertexShader)

            // add the fragment shader to program
            GLES20.glAttachShader(it, fragmentShader)

            // creates OpenGL ES program executables
            GLES20.glLinkProgram(it)

            // Add program to OpenGL environment
            GLES32.glUseProgram(it)
        }

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES32.glGetAttribLocation(mProgram, "aVertexPosition")
        // Enable a handle to the triangle vertices
        GLES32.glEnableVertexAttribArray(mPositionHandle)
        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES32.glGetUniformLocation(mProgram, "uMVPMatrix")
        mColorHandle = GLES32.glGetAttribLocation(mProgram, "aVertexColor")
        GLES32.glEnableVertexAttribArray(mColorHandle)
        mPointLightLocationHandle = GLES32.glGetUniformLocation(mProgram, "uPointLightingLocation")
        MyRenderer.checkGlError("glGetUniformLocation")

        CoroutineScope(Dispatchers.IO).launch {
            val obj = ObjLoader(context, "number0.obj")
            charAVertex = obj.vertexArray
            charAColor = obj.textureCoordinates
            charAIndices = obj.indexArray.toTypedArray()
            launch {
                cubeBuffer = ByteBuffer.allocateDirect(obj.vertexArray.size * 4).run {
                    // use the device hardware's native byte order
                    order(ByteOrder.nativeOrder())
                    // create a floating point buffer from the ByteBuffer
                    asFloatBuffer().apply {
                        // add the coordinates to the FloatBuffer
                        put(obj.vertexArray)
                        // set the buffer to read the first coordinate
                        position(0)
                    }
                }    // initialize vertex byte buff }
                launch {
                    cubeColorBuffer =
                        ByteBuffer.allocateDirect(obj.textureCoordinates.size * 4).run {
                            // use the device hardware's native byte order
                            order(ByteOrder.nativeOrder())
                            // create a floating point buffer from the ByteBuffer
                            asFloatBuffer().apply {
                                // add the coordinates to the FloatBuffer
                                put(obj.textureCoordinates)
                                // set the buffer to read the first coordinate
                                position(0)
                            }
                        }
                }
                launch {
                    indexBuffer = IntBuffer.allocate(obj.indexArray.size).apply {
                        put(obj.indexArray)
                        position(0)
                    }
                }
            }
        }

    }

    fun draw(mvpMatrix: FloatArray?) {
        // Apply the projection and view transformation
        GLES32.glUniform3fv(mPointLightLocationHandle, 1, lightLocation, 0)
        GLES32.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix?.copyOf(), 0)
        MyRenderer.checkGlError("glUniformMatrix4fv")
        //set the attribute of the vertex to point to the vertex buffer
        GLES32.glVertexAttribPointer(
            mPositionHandle, COORDS_PER_VERTEX,
            GLES32.GL_FLOAT, false, vertexStride, cubeBuffer
        )
        GLES32.glVertexAttribPointer(
            mColorHandle, COLOUR_PER_VERTEX,
            GLES32.GL_FLOAT, false, colorStride, cubeColorBuffer
        )
//        GLES32.glDrawArrays(GLES32.GL_TRIANGLES, 0, vertexStride)
        //draw the cube
        GLES32.glDrawElements(
            GLES32.GL_POINTS,
            charAIndices.size,
            GLES32.GL_UNSIGNED_INT,
            indexBuffer
        )

    }

    companion object {
        // number of coordinates per vertex in this array
        const val COORDS_PER_VERTEX: Int = 3
        const val COLOUR_PER_VERTEX: Int = 4
        var charAVertex: FloatArray = floatArrayOf(
            -0.2f, 1.0f, -0.3f,
            -0.2f, 1.0f, 0.3f,
            0.2f, 1.0f, -0.3f,
            0.2f, 1.0f, 0.3f,
            -1.0f, -1.0f, -0.5f,
            -1.0f, -1.0f, 0.5f,
            -0.6f, -1.0f, -0.5f,
            -0.6f, -1.0f, 0.5f,
            0.6f, -1.0f, 0.5f,
            0.6f, -1.0f, -0.5f,
            1.0f, -1.0f, 0.5f,
            1.0f, -1.0f, -0.5f,
            0.0f, 0.8f, 0.3f,
            0.0f, 0.8f, -0.3f,
            0.25f, 0.1f, 0.382f,
            0.25f, 0.1f, -0.382f,
            -0.25f, 0.1f, 0.382f,
            -0.25f, 0.1f, -0.382f,
            0.32f, -0.1f, 0.41f,
            0.32f, -0.1f, -0.41f,
            -0.32f, -0.1f, 0.41f,
            -0.32f, -0.1f, -0.41f
        )
        var charAIndices = arrayOf(
            0, 1, 2, 2, 3, 1,
            0, 4, 5, 5, 1, 0,
            4, 5, 6, 6, 7, 5,
            1, 5, 7, 7, 3, 1,
            0, 4, 6, 6, 2, 0,
            3, 10, 11, 11, 3, 2,
            8, 9, 10, 10, 11, 9,
            3, 10, 8, 8, 3, 1,
            2, 11, 9, 9, 2, 0,
            12, 13, 6, 6, 7, 12,
            12, 8, 9, 9, 13, 12,
            14, 15, 16, 16, 17, 15,
            19, 18, 20, 20, 21, 19,
            14, 18, 20, 20, 16, 14,
            15, 19, 21, 21, 17, 15
        )

        var charAColor = floatArrayOf(
            0.0f, 0.0f, 1.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f
        )
    }

}