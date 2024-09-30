package com.example.opengles.shapes

import android.opengl.GLES20
import android.opengl.GLES32
import com.example.opengles.MyRenderer
import com.example.opengles.Utils.Companion.loadShader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.cos
import kotlin.math.sin

class Triangle2D {
    // Define the colors
    private val fillColor = floatArrayOf(1f, 1f, 0f, 0f)  // red
    private val borderColor = floatArrayOf(1.0f, 0.0f, 1f, 0f)  // green

    private val vertexShaderCode = "attribute vec3 aVertexPosition;" +
            "uniform mat4 uMVPMatrix;" +
//            "uniform vec4 vColor;" +
            "void main() {" +
            "gl_Position = uMVPMatrix * vec4(aVertexPosition,1.0);" +
//            "gl_PointSize = 40.0;"+
//            "vColor=vec4(1.0,0.0,1.0,0.0);" +
            "}"
    private val fragmentShaderCode = "precision mediump float;" +
            "uniform vec4 vColor; " +
            "void main() {gl_FragColor = vColor;}"

    // initialize vertex byte buffer for shape coordinates
    private val vertexBuffer: FloatBuffer = ByteBuffer.allocateDirect(squareVertex.size * 4).run {
        // use the device hardware's native byte order
        order(ByteOrder.nativeOrder())
        // create a floating point buffer from the ByteBuffer
        asFloatBuffer().apply {
            // add the coordinates to the FloatBuffer
            put(squareVertex)
            // set the buffer to read the first coordinate
            position(0)
        }
    }    // initialize vertex byte buffer for shape coordinates
    private val vertexBorderBuffer: FloatBuffer = ByteBuffer.allocateDirect(border.size * 4).run {
        // use the device hardware's native byte order
        order(ByteOrder.nativeOrder())
        // create a floating point buffer from the ByteBuffer
        asFloatBuffer().apply {
            // add the coordinates to the FloatBuffer
            put(border)
            // set the buffer to read the first coordinate
            position(0)
        }
    }
    private val mProgram: Int
    private val mPositionHandle: Int
    private val mMVPMatrixHandle: Int
    private val mColorHandle: Int
    private val vertexCount: Int = squareVertex.size / COORDS_PER_VERTEX // number of vertices
    private val vertexStride: Int = COORDS_PER_VERTEX * 4 // 4 bytes per vertex

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
        mColorHandle = GLES32.glGetUniformLocation(mProgram,"vColor")
        MyRenderer.checkGlError("glGetUniformLocation")
    }

    fun draw(mvpMatrix: FloatArray?) {
        // Apply the projection and view transformation
        GLES32.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0)
        MyRenderer.checkGlError("glUniformMatrix4fv")
        //set the attribute of the vertex to point to the vertex buffer
        GLES32.glVertexAttribPointer(
            mPositionHandle, COORDS_PER_VERTEX,
            GLES32.GL_FLOAT, false, vertexStride, vertexBuffer
        )
        // Draw the triangle
        //GL_TRIANGLE_STRIP -> connect the triangles
        //GL_TRIANGLES -> need 3 vertices for each triangle
        //GL_TRIANGLE_FAN -> best suited for circles
        GLES32.glUniform4fv(mColorHandle, 1, fillColor, 0)
        GLES32.glDrawArrays(GLES32.GL_TRIANGLE_FAN, 0, vertexCount)
        GLES32.glVertexAttribPointer(
            mPositionHandle, COORDS_PER_VERTEX,
            GLES32.GL_FLOAT, false, vertexStride, vertexBorderBuffer
        )
        GLES32.glUniform4fv(mColorHandle, 1, borderColor, 0)
        GLES32.glDrawArrays(GLES32.GL_LINE_LOOP, 0, border.size/3)
    }


    companion object {
        // number of coordinates per vertex in this array
        const val COORDS_PER_VERTEX: Int = 3
        var triangleVertex: FloatArray = floatArrayOf(
            // Front face (z = 1.0)
            -1.0f, -1.0f, 1.0f,  // Bottom-left
            1.0f, -1.0f, 1.0f,   // Bottom-right
            1.0f, 1.0f, 1.0f,    // Top-right
            -1.0f, 1.0f, 1.0f,   // Top-left

            // Back face (z = -1.0)
            -1.0f, -1.0f, -1.0f,  // Bottom-left
            1.0f, -1.0f, -1.0f,   // Bottom-right
            1.0f, 1.0f, -1.0f,    // Top-right
            -1.0f, 1.0f, -1.0f    // Top-left
        )
        var squareVertex: FloatArray = generateEllipseVertices()
        private fun generateEllipseVertices(cx: Float = 0f, cy: Float = 0f, a: Float = 5.5f, b: Float = 12.3f, numVertices: Int = 360): FloatArray {
            val vertices = FloatArray((numVertices + 2) * 3) // +2 for the center and wrap-around vertex
            vertices[0] = cx // Center x
            vertices[1] = cy // Center y
            vertices[2] = 0.0f // Center z

            for (i in 1..numVertices + 1) {
                val angle = (2.0 * Math.PI * (i - 1) / numVertices).toFloat()
                vertices[3 * i] = cx + a * cos(angle.toDouble()).toFloat() // x-coordinate
                vertices[3 * i + 1] = cy + b * sin(angle.toDouble()).toFloat() // y-coordinate
                vertices[3 * i + 2] = 0.0f // z-coordinate
            }
            return vertices
        }
        val border = generateEllipseBorder()
        private fun generateEllipseBorder(cx: Float = 0f, cy: Float = 0f, a: Float = 5.5f, b: Float = 12.3f, numVertices: Int = 360): FloatArray {
            val vertices = FloatArray((numVertices + 2) * 3) // +2 for the center and wrap-around vertex
//            vertices[0] = cx // Center x
//            vertices[1] = cy // Center y
//            vertices[2] = 0.0f // Center z

            for (i in 0..numVertices + 1) {
                val angle = (2.0 * Math.PI * (i - 1) / numVertices).toFloat()
                vertices[3 * i] = cx + a * cos(angle.toDouble()).toFloat() // x-coordinate
                vertices[3 * i + 1] = cy + b * sin(angle.toDouble()).toFloat() // y-coordinate
                vertices[3 * i + 2] = 0.0f // z-coordinate
            }
            return vertices
        }
    }
}