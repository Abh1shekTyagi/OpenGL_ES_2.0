package com.example.opengles.shapes

import android.opengl.GLES20
import android.opengl.GLES32
import com.example.opengles.MyRenderer
import com.example.opengles.Utils.Companion.loadShader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class Pyramid {

    private val mProgram: Int
    private val mPositionHandle: Int
    private val mMVPMatrixHandle: Int
    private val mColorHandle: Int
    private val vertexShaderCode =
        "attribute vec3 aVertexPosition;" +
                "attribute vec4 aVertexColor;" +
                "uniform mat4 uMVPMatrix;" +
                "varying vec4 vColor;" +
                "void main() {" +
                "gl_Position = uMVPMatrix * vec4(aVertexPosition,1.0);" +
//            "gl_PointSize = 40.0;"+
                "vColor = aVertexColor;" + //RGBA
                "}"
    private val fragmentShaderCode = "precision mediump float;" +
            "varying vec4 vColor; " +
            "void main() {gl_FragColor = vColor;}"

    // initialize vertex byte buffer for shape coordinates
    private val pyramidBuffer: FloatBuffer = ByteBuffer.allocateDirect(pyramidVertex.size * 4).run {
        // use the device hardware's native byte order
        order(ByteOrder.nativeOrder())
        // create a floating point buffer from the ByteBuffer
        asFloatBuffer().apply {
            // add the coordinates to the FloatBuffer
            put(pyramidVertex)
            // set the buffer to read the first coordinate
            position(0)
        }
    }    // initialize vertex byte buffer for shape coordinates

    private val pyramidColorBuffer: FloatBuffer =
        ByteBuffer.allocateDirect(pyramidColor.size * 4).run {
            // use the device hardware's native byte order
            order(ByteOrder.nativeOrder())
            // create a floating point buffer from the ByteBuffer
            asFloatBuffer().apply {
                // add the coordinates to the FloatBuffer
                put(pyramidColor)
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
        MyRenderer.checkGlError("glGetUniformLocation")
    }

    fun draw(mvpMatrix: FloatArray?) {
        // Apply the projection and view transformation
        GLES32.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0)
        MyRenderer.checkGlError("glUniformMatrix4fv")
        //set the attribute of the vertex to point to the vertex buffer
        GLES32.glVertexAttribPointer(
            mPositionHandle, COORDS_PER_VERTEX,
            GLES32.GL_FLOAT, false, vertexStride, pyramidBuffer
        )
        GLES32.glVertexAttribPointer(
            mColorHandle, COLOUR_PER_VERTEX,
            GLES32.GL_FLOAT, false, colorStride, pyramidColorBuffer
        )
        GLES32.glDrawArrays(GLES32.GL_TRIANGLES, 0, vertexStride)
    }


    companion object {
        // number of coordinates per vertex in this array
        const val COORDS_PER_VERTEX: Int = 3
        const val COLOUR_PER_VERTEX: Int = 4
        val pyramidVertex: FloatArray = floatArrayOf(
            // Front face
            0f, 1f, 0f,
            -1f, -1f, 1f,
            1f, -1f, 1f,

            //right face
            0f, 1f, 0f,
            1f, -1f, 1f,
            1f, -1f, -1f,

            //back face
            0f, 1f, 0f,
            1f, -1f, -1f,
            -1f, -1f, -1f,

            //left face

            0f, 1f, 0f,
            -1f, -1f, -1f,
            -1f, -1f, 1f
            //bottom is not shown so we do not create it.
        )

        val pyramidColor = floatArrayOf(
            //front face
            1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,
            //right face
            1.0f, 0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            //back face
            1.0f, 0.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,
            //left face
            1.0f, 0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f
        )
    }
}