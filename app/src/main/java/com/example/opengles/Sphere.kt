package com.example.opengles

import android.opengl.GLES32
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin


class Sphere {
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
    private var vertexBuffer: FloatBuffer? = null
    private var colorBuffer: FloatBuffer? = null
    private var indexBuffer: IntBuffer? = null
    private var mProgram = 0
    private var mPositionHandle = 0
    private var mColorHandle: Int = 0
    private var mMVPMatrixHandle = 0
    private var pointLightLocationHandle = 0
    // number of coordinates per vertex in this array

    private var lightLocation = Array<Float>(3) { 0f }

    val COORDS_PER_VERTEX: Int = 3 // number of coordinates per vertex in this array

    val COLOR_PER_VERTEX: Int = 4
    private val vertexStride = COORDS_PER_VERTEX * 4 // 4 bytes per vertex
    private val colorStride = COLOR_PER_VERTEX * 4

    private lateinit var sphereVertex: FloatArray

    private lateinit var sphereColor: FloatArray

    private lateinit var sphereIndex: IntArray

    private fun createShpere(radius: Float, nolatitude: Int, nolongitude: Int) {
        val vertices = FloatArray(65535)
        val pIndex = IntArray(65535)
        val pcolor = FloatArray(65535)
        var vertexindex = 0
        var colorindex = 0
        var indx = 0
        val dist = 0f
        for (row in 0..nolatitude) {
            val theta = row * Math.PI / nolatitude
            val sinTheta = sin(theta)
            val cosTheta = cos(theta)
            var tcolor = -0.5f
            val tcolorinc = 1f / (nolongitude + 1).toFloat()
            for (col in 0..nolongitude) {
                val phi = col * 2 * Math.PI / nolongitude
                val sinPhi = sin(phi)
                val cosPhi = cos(phi)
                val x = cosPhi * sinTheta
                val y = cosTheta
                val z = sinPhi * sinTheta
                vertices[vertexindex++] = (radius * x).toFloat()
                vertices[vertexindex++] = (radius * y).toFloat() + dist
                vertices[vertexindex++] = (radius * z).toFloat()
                pcolor[colorindex++] = 1f
                pcolor[colorindex++] = abs(tcolor.toDouble()).toFloat()
                pcolor[colorindex++] = 0f
                pcolor[colorindex++] = 1f
                tcolor += tcolorinc
            }
        }
        for (row in 0 until nolatitude) {
            for (col in 0 until nolongitude) {
                val first = (row * (nolongitude + 1)) + col
                val second = first + nolongitude + 1
                pIndex[indx++] = first
                pIndex[indx++] = second
                pIndex[indx++] = first + 1
                pIndex[indx++] = second
                pIndex[indx++] = second + 1
                pIndex[indx++] = first + 1
            }
        }

        sphereVertex = vertices.copyOf(vertexindex)
        sphereIndex = pIndex.copyOf(indx)
        sphereColor = pcolor.copyOf(colorindex)
    }

    init {
        lightLocation[0] = 3f; lightLocation[1] = 3f; lightLocation[2] = 0f
        createShpere(2f, 30, 30)
        // initialize vertex byte buffer for shape coordinates
        vertexBuffer =
            ByteBuffer.allocateDirect(sphereVertex.size * 4).run {
                order(ByteOrder.nativeOrder())
                asFloatBuffer().apply {
                    put(sphereVertex)
                    position(0)
                }
            }

        colorBuffer =
            ByteBuffer.allocateDirect(sphereColor.size * 4).run {
                order(ByteOrder.nativeOrder())
                asFloatBuffer().apply {
                    put(sphereColor)
                    position(0)
                }
            } // (# of coordinate values * 4 bytes per float)
        indexBuffer = IntBuffer.allocate(sphereIndex.size).apply {
            put(sphereIndex)
            position(0)
        }
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
        // Prepare the triangle coordinate data
        GLES32.glVertexAttribPointer(
            mPositionHandle,
            COORDS_PER_VERTEX,
            GLES32.GL_FLOAT,
            false,
            vertexStride,
            vertexBuffer
        )
        //get the handle to vertex shader's aVertexColor member
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
        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES32.glGetUniformLocation(mProgram, "uMVPMatrix")
        pointLightLocationHandle = GLES32.glGetUniformLocation(mProgram, "uPointLightingLocation")
    }

    fun draw(mvpMatrix: FloatArray?) {
        // Apply the projection and view transformation
        GLES32.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0)
        GLES32.glUniform3fv(pointLightLocationHandle, 1, lightLocation.toFloatArray(), 0)
        MyRenderer.checkGlError("glUniformMatrix4fv")
        //===================
        //set the attribute of the vertex to point to the vertex buffer
        GLES32.glVertexAttribPointer(
            mPositionHandle, COORDS_PER_VERTEX,
            GLES32.GL_FLOAT, false, vertexStride, vertexBuffer
        )
        GLES32.glVertexAttribPointer(
            mColorHandle, COLOR_PER_VERTEX,
            GLES32.GL_FLOAT, false, colorStride, colorBuffer
        )
        // Draw the sphere
        GLES32.glDrawElements(
            GLES32.GL_TRIANGLES,
            sphereIndex.size,
            GLES32.GL_UNSIGNED_INT,
            indexBuffer
        )
    }
}