package com.example.opengles

import android.opengl.GLES32
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin


class ArbitraryShape {
    private val vertexShaderCode = "attribute vec3 aVertexPosition;" +  //vertex of an object
            "attribute vec4 aVertexColor;" +  //the colour  of the object
            "uniform mat4 uMVPMatrix;" +  //model view  projection matrix
            "varying vec4 vColor;" +  //variable to be accessed by the fragment shader
            "void main() {" +
            "gl_Position = uMVPMatrix* vec4(aVertexPosition, 1.0);" +  //calculate the position of the vertex
            "vColor=aVertexColor;}" //get the colour from the application program
    private val fragmentShaderCode = "precision mediump float;" +  //define the precision of float
            "varying vec4 vColor;" +  //variable from the vertex shader
            //---------
            "void main() {" +
            "   gl_FragColor = vColor; }" //change the colour based on the variable from the vertex shader
    private var vertexBuffer: FloatBuffer? = null
    private var colorBuffer: FloatBuffer? = null
    private var indexBuffer: IntBuffer? = null
    private var vertex2Buffer: FloatBuffer? = null
    private var color2Buffer: FloatBuffer? = null
    private var index2Buffer: IntBuffer? = null
    private var ringVertexBuffer: FloatBuffer? = null
    private var ringColorBuffer: FloatBuffer? = null
    private var ringIndexBuffer: IntBuffer? = null
    private var mProgram = 0
    private var mPositionHandle = 0
    private var mColorHandle: Int = 0
    private var mMVPMatrixHandle = 0
    //---------
    // number of coordinates per vertex in this array
    val COORDS_PER_VERTEX: Int = 3

    val COLOR_PER_VERTEX: Int = 4
    private val vertexStride = COORDS_PER_VERTEX * 4 // 4 bytes per vertex
    private val colorStride = COLOR_PER_VERTEX * 4 //4 bytes per vertex

    private lateinit var sphereVertex: FloatArray

    private lateinit var sphereIndex: IntArray

    private lateinit var sphereColor: FloatArray
    //2nd sphere

    private lateinit var sphere2Vertex: FloatArray

    private lateinit var sphere2Index: IntArray

    private lateinit var sphere2Color: FloatArray
    //ring

    private lateinit var ringVertex: FloatArray

    private lateinit var ringIndex: IntArray

    private lateinit var ringColor: FloatArray

    var lightLocation: FloatArray = FloatArray(3) //point light source location

    private fun createSphere(radius: Float, nolatitude: Int, nolongitude: Int) {
        val vertices = FloatArray(65535)
        val index = IntArray(65535)
        val color = FloatArray(65535)
        val pnormlen = (nolongitude + 1) * 3 * 3
        var vertexindex = 0
        var colorindex = 0
        var indx = 0
        val vertices2 = FloatArray(65535)
        val index2 = IntArray(65535)
        val color2 = FloatArray(65525)
        var vertex2index = 0
        var color2index = 0
        var indx2 = 0
        val ring_vertices = FloatArray(65535)
        val ring_index = IntArray(65535)
        val ring_color = FloatArray(65525)
        var rvindx = 0
        var rcindex = 0
        var rindx = 0
        val dist = 3f
        var plen = (nolongitude + 1) * 3 * 3
        var pcolorlen = (nolongitude + 1) * 4 * 3
        for (row in 0 until nolatitude + 1) {
            val theta = row * Math.PI / nolatitude
            val sinTheta = sin(theta)
            val cosTheta = cos(theta)
            var tcolor = -0.5f
            val tcolorinc = 1 / (nolongitude + 1).toFloat()
            for (col in 0 until nolongitude + 1) {
                val phi = col * 2 * Math.PI / nolongitude
                val sinPhi = sin(phi)
                val cosPhi = cos(phi)
                val x = cosPhi * sinTheta
                val y = cosTheta
                val z = sinPhi * sinTheta
                vertices[vertexindex++] = (radius * x).toFloat()
                vertices[vertexindex++] = (radius * y).toFloat() + dist
                vertices[vertexindex++] = (radius * z).toFloat()

                vertices2[vertex2index++] = (radius * x).toFloat()
                vertices2[vertex2index++] = (radius * y).toFloat() - dist
                vertices2[vertex2index++] = (radius * z).toFloat()

                color[colorindex++] = 1f
                color[colorindex++] = abs(tcolor.toDouble()).toFloat()
                color[colorindex++] = 0f
                color[colorindex++] = 1f

                color2[color2index++] = 0f
                color2[color2index++] = 1f
                color2[color2index++] = abs(tcolor.toDouble()).toFloat()
                color2[color2index++] = 1f

                if (row == 20) {
                    ring_vertices[rvindx++] = (radius * x).toFloat()
                    ring_vertices[rvindx++] = (radius * y).toFloat() + dist
                    ring_vertices[rvindx++] = (radius * z).toFloat()
                    ring_color[rcindex++] = 1f
                    ring_color[rcindex++] = abs(tcolor.toDouble()).toFloat()
                    ring_color[rcindex++] = 0f
                    ring_color[rcindex++] = 1f
                }
                if (row == 15) {
                    ring_vertices[rvindx++] = (radius * x).toFloat() / 2
                    ring_vertices[rvindx++] = (radius * y).toFloat() / 2 + 0.2f * dist
                    ring_vertices[rvindx++] = (radius * z).toFloat() / 2
                    ring_color[rcindex++] = 1f
                    ring_color[rcindex++] = abs(tcolor.toDouble()).toFloat()
                    ring_color[rcindex++] = 0f
                    ring_color[rcindex++] = 1f
                }
                if (row == 10) {
                    ring_vertices[rvindx++] = (radius * x).toFloat() / 2
                    ring_vertices[rvindx++] = (radius * y).toFloat() / 2 - 0.1f * dist
                    ring_vertices[rvindx++] = (radius * z).toFloat() / 2
                    ring_color[rcindex++] = 0f
                    ring_color[rcindex++] = 1f
                    ring_color[rcindex++] = abs(tcolor.toDouble()).toFloat()
                    ring_color[rcindex++] = 1f
                }
                if (row == 20) {
                    ring_vertices[plen++] = (radius * x).toFloat()
                    ring_vertices[plen++] = (-radius * y).toFloat() - dist
                    ring_vertices[plen++] = (radius * z).toFloat()
                    ring_color[pcolorlen++] = 0f
                    ring_color[pcolorlen++] = 1f
                    ring_color[pcolorlen++] = abs(tcolor.toDouble()).toFloat()
                    ring_color[pcolorlen++] = 1f
                    //-------
                }
                tcolor += tcolorinc
            }
        }
        //index buffer
        for (row in 0 until nolatitude) {
            for (col in 0 until nolongitude) {
                val P0 = (row * (nolongitude + 1)) + col
                val P1 = P0 + nolongitude + 1
                index[indx++] = P1
                index[indx++] = P0
                index[indx++] = P0 + 1
                index[indx++] = P1 + 1
                index[indx++] = P1
                index[indx++] = P0 + 1

                index2[indx2++] = P1
                index2[indx2++] = P0
                index2[indx2++] = P0 + 1
                index2[indx2++] = P1 + 1
                index2[indx2++] = P1
                index2[indx2++] = P0 + 1
            }
        }
        rvindx = (nolongitude + 1) * 3 * 4
        rcindex = (nolongitude + 1) * 4 * 4
        plen = nolongitude + 1
        for (j in 0 until plen - 1) {
            ring_index[rindx++] = j
            ring_index[rindx++] = j + plen
            ring_index[rindx++] = j + 1
            ring_index[rindx++] = j + plen + 1
            ring_index[rindx++] = j + 1
            ring_index[rindx++] = j + plen

            ring_index[rindx++] = j + plen
            ring_index[rindx++] = j + plen * 2
            ring_index[rindx++] = j + plen + 1
            ring_index[rindx++] = j + plen * 2 + 1
            ring_index[rindx++] = j + plen + 1
            ring_index[rindx++] = j + plen * 2

            ring_index[rindx++] = j + plen * 3
            ring_index[rindx++] = j
            ring_index[rindx++] = j + 1
            ring_index[rindx++] = j + 1
            ring_index[rindx++] = j + plen * 3 + 1
            ring_index[rindx++] = j + plen * 3
        }


        //set the buffers
        sphereVertex = vertices.copyOf(vertexindex)
        sphereIndex = index.copyOf(indx)
        sphereColor = color.copyOf(colorindex)
        sphere2Vertex = vertices2.copyOf(vertex2index)
        sphere2Index = index2.copyOf(indx2)
        sphere2Color = color2.copyOf(color2index)
        ringVertex = ring_vertices.copyOf(rvindx)
        ringColor = ring_color.copyOf(rcindex)
        ringIndex = ring_index.copyOf(rindx)
    }

    init {
        createSphere(2f, 30, 30)
        // initialize vertex byte buffer for shape coordinates
        vertexBuffer = ByteBuffer.allocateDirect(sphereVertex.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(sphereVertex)
                position(0)
            }
        }
        colorBuffer = ByteBuffer.allocateDirect(sphereColor.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(sphereColor)
                position(0)
            }
        }
        indexBuffer = IntBuffer.allocate(sphereIndex.size).apply {
            put(sphereIndex)
            position(0)
        }
        //2nd sphere
        vertex2Buffer = ByteBuffer.allocateDirect(sphere2Vertex.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(sphere2Vertex)
                position(0)
            }
        }
        color2Buffer = ByteBuffer.allocateDirect(sphere2Color.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(sphere2Color)
                position(0)
            }
        }
        index2Buffer = IntBuffer.allocate(sphereIndex.size).apply {
            put(sphereIndex)
            position(0)
        }
        ringVertexBuffer = ByteBuffer.allocateDirect(ringVertex.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(ringVertex)
                position(0)
            }
        }
        ringColorBuffer = ByteBuffer.allocateDirect(ringColor.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(ringColor)
                position(0)
            }
        }
        ringIndexBuffer = IntBuffer.allocate(ringIndex.size).apply {
            put(ringIndex)
            position(0)
        }
        //----------
        // prepare shaders and OpenGL program
        val vertexShader = MyRenderer.loadShader(GLES32.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = MyRenderer.loadShader(GLES32.GL_FRAGMENT_SHADER, fragmentShaderCode)
        mProgram = GLES32.glCreateProgram() // create empty OpenGL Program
        GLES32.glAttachShader(mProgram, vertexShader) // add the vertex shader to program
        GLES32.glAttachShader(mProgram, fragmentShader) // add the fragment shader to program
        GLES32.glLinkProgram(mProgram) // link the  OpenGL program to create an executable
        GLES32.glUseProgram(mProgram) // Add program to OpenGL environment
        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES32.glGetAttribLocation(mProgram, "aVertexPosition")
        // Enable a handle to the triangle vertices
        GLES32.glEnableVertexAttribArray(mPositionHandle)
        mColorHandle = GLES32.glGetAttribLocation(mProgram, "aVertexColor")
        // Enable a handle to the  colour
        GLES32.glEnableVertexAttribArray(mColorHandle)
        // Prepare the colour coordinate data
        GLES32.glVertexAttribPointer(
            mColorHandle,
            COLOR_PER_VERTEX,
            GLES32.GL_FLOAT,
            false,
            colorStride,
            colorBuffer
        )
        //---------
        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES32.glGetUniformLocation(mProgram, "uMVPMatrix")
    }

    fun draw(mvpMatrix: FloatArray?) {
        // Apply the projection and view transformation
        GLES32.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0)
        //---------
        //set the attribute of the vertex to point to the vertex buffer
        GLES32.glVertexAttribPointer(
            mPositionHandle, COORDS_PER_VERTEX,
            GLES32.GL_FLOAT, false, vertexStride, vertexBuffer
        )
        GLES32.glVertexAttribPointer(
            mColorHandle, COORDS_PER_VERTEX,
            GLES32.GL_FLOAT, false, colorStride, colorBuffer
        )
        // Draw the Sphere
        GLES32.glDrawElements(
            GLES32.GL_TRIANGLES,
            sphereIndex.size,
            GLES32.GL_UNSIGNED_INT,
            indexBuffer
        )
        //---------
        //2nd sphere
        GLES32.glVertexAttribPointer(
            mPositionHandle, COORDS_PER_VERTEX,
            GLES32.GL_FLOAT, false, vertexStride, vertex2Buffer
        )
        GLES32.glVertexAttribPointer(
            mColorHandle, COORDS_PER_VERTEX,
            GLES32.GL_FLOAT, false, colorStride, color2Buffer
        )
        // Draw the Sphere
        GLES32.glDrawElements(
            GLES32.GL_TRIANGLES,
            sphere2Index.size,
            GLES32.GL_UNSIGNED_INT,
            index2Buffer
        )
        ///////////////////
        //Rings
        GLES32.glVertexAttribPointer(
            mPositionHandle, COORDS_PER_VERTEX,
            GLES32.GL_FLOAT, false, vertexStride, ringVertexBuffer
        )
        GLES32.glVertexAttribPointer(
            mColorHandle, COORDS_PER_VERTEX,
            GLES32.GL_FLOAT, false, colorStride, ringColorBuffer
        )
        GLES32.glDrawElements(
            GLES32.GL_TRIANGLES,
            ringIndex.size,
            GLES32.GL_UNSIGNED_INT,
            ringIndexBuffer
        )
    }

}