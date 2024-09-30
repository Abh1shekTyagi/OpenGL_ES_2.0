package com.example.opengles

import android.opengl.GLES32
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin


class HalfCone {
    private val vertexShaderCode =
        "attribute vec3 vPosition;" +  //vertex of an object (passed from the java  program)
                "attribute vec4 aVertexColor;" +  //color
                "uniform mat4 uMVPMatrix;" +  //model view project matrix
                "varying vec4 vColor;" +  //colour of the object
                "void main(void) {" +
                "gl_Position = uMVPMatrix * vec4(vPosition,1.0);" +  //position of the vertex
                "vColor=aVertexColor;" +  //colour of the vertx
                "}"

    private val fragmentShaderCode = "precision mediump float;" +  //define the precision of float
            "varying vec4 vColor;" +  //color of the pixel
            "void main(void) {" +
            "gl_FragColor=vec4(vColor.xyz,1);" +
            "}"
    private var vertexBuffer: FloatBuffer? = null;

    private var colorBuffer: FloatBuffer? = null
    private var mProgram = 0
    private var mPositionHandle = 0
    private var mColorHandle = 0
    private var mMVPMatrixHandle = 0

    // number of coordinates per vertex in this array

    //private final int vertexCount = triangleCoords.length / COORDS_PER_VERTEX;
    private val vertexStride = COORDS_PER_VERTEX * 4 // 4 bytes per vertex

    //////////////////////////////
    private val MAX_NO_VERTICES = 65536
    private val circle_radius = 1f
    private val inner_color = floatArrayOf(1f, 0f, 0f, 1f)
    private val outer_color = floatArrayOf(1f, 0f, 1f, 0f)
    private val circle_linewidth = 0.3f //size differences between the inner and outer cicles
    private val circle_resolution = 5f
    private lateinit var circle_vertices: FloatArray
    private lateinit var circle_color: FloatArray
    private var vertexCount = 0
    private val COLOR_PER_VERTEX = 4 //RGBA
    private val ColorStride = COLOR_PER_VERTEX * 4 //4 bytes per float
    private fun SetColorBuffer(
        colorBuffer1: FloatArray,
        curIndexParams: Int,
        color: FloatArray
    ): Int { //copy the color into the color buffer
        //color_buffer : the color buffer
        //curindex: index of the color buffer
        //color: color of the vertex [r,g,b,a]
        var curindex = curIndexParams
        colorBuffer1[curindex++] = color[0]
        colorBuffer1[curindex++] = color[1]
        colorBuffer1[curindex++] = color[2]
        colorBuffer1[curindex++] = color[3]
        return curindex
    }

    private fun initBufferCircleQuadrant(
        vertexbuf: FloatArray, vertexIndex: Int,
        colorbuf: FloatArray,
        radius: Float, angleIndex: Float,
        qx: Float, qy: Float, linewidth: Float,
        x_offset: Float, y_offset: Float,
        inner_color: FloatArray, outer_color: FloatArray,
        inner_depth: Float, outer_depth: Float
    ): Int { //create the points for creating a quarter of the circle
        //vertexbuf -> buffer to store the vertex coordinates
        //vertex_index -> index the vertex buffer
        //colorbuf -> buffer to store the color of the vertices
        //radius -> radius of the circle
        // qx,qy are the signs to identify the specific quadrant
        //x_offset,y_offset -> x and y translation of the circle quadrant
        //inner_depth and outer_depth -> z coordinates the inner and outer vertices

        //first triangle strip to draw the thick line

        var vertex_index = vertexIndex
        var angle = (angleIndex / 180 * PI.toFloat())
        var x = (radius * cos(angle))
        var y = (radius * sin(angle))
        var color_circle_index = vertex_index / 3 * 4
        vertexbuf[vertex_index++] = x * qx + x_offset
        vertexbuf[vertex_index++] = y * qy + y_offset
        vertexbuf[vertex_index++] = outer_depth
        color_circle_index = SetColorBuffer(colorbuf, color_circle_index, outer_color)
        x = ((radius - linewidth) * cos(angle)) as Float
        y = ((radius - linewidth) * sin(angle)) as Float
        vertexbuf[vertex_index++] = x * qx + x_offset
        vertexbuf[vertex_index++] = y * qy + y_offset
        vertexbuf[vertex_index++] = inner_depth
        color_circle_index = SetColorBuffer(colorbuf, color_circle_index, inner_color)
        angle = ((angleIndex + circle_resolution) / 180 * PI.toFloat())
        x = (radius * cos(angle))
        y = (radius * sin(angle))
        vertexbuf[vertex_index++] = x * qx + x_offset
        vertexbuf[vertex_index++] = y * qy + y_offset
        vertexbuf[vertex_index++] = outer_depth
        color_circle_index = SetColorBuffer(colorbuf, color_circle_index, outer_color)
        //second triangle to complete the strip
        vertexbuf[vertex_index++] = x * qx + x_offset
        vertexbuf[vertex_index++] = y * qy + y_offset
        vertexbuf[vertex_index++] = outer_depth
        color_circle_index = SetColorBuffer(colorbuf, color_circle_index, outer_color)
        angle = ((angleIndex + circle_resolution) / 180 * PI.toFloat())
        x = ((radius - linewidth) * cos(angle))
        y = ((radius - linewidth) * sin(angle))
        vertexbuf[vertex_index++] = x * qx + x_offset
        vertexbuf[vertex_index++] = y * qy + y_offset
        vertexbuf[vertex_index++] = inner_depth
        color_circle_index = SetColorBuffer(colorbuf, color_circle_index, inner_color)
        angle = (angleIndex / 180 * PI.toFloat())
        x = ((radius - linewidth) * cos(angle))
        y = ((radius - linewidth) * sin(angle))
        vertexbuf[vertex_index++] = x * qx + x_offset
        vertexbuf[vertex_index++] = y * qy + y_offset
        vertexbuf[vertex_index++] = inner_depth
        color_circle_index = SetColorBuffer(colorbuf, color_circle_index, inner_color)
        return vertex_index
    }


    private fun init_circle_vertices() {   //initialise vertices and color buffer for drawing a circle
        var circleIndex = 0 //vertices indices
        circle_vertices = FloatArray(MAX_NO_VERTICES * COORDS_PER_VERTEX)
        circle_color = FloatArray(MAX_NO_VERTICES * COLOR_PER_VERTEX)
        //hollow circle
        val inner_depth = 0f
        val outer_depth = 1.0f
        val x_offset = 0f
        val y_offset = 0f
        run {
            var i = 0
            while (i < 90) {
                //first Quadrant
                circleIndex = initBufferCircleQuadrant(
                    circle_vertices,
                    circleIndex,
                    circle_color,
                    circle_radius,
                    i.toFloat(),
                    1f,
                    1f,
                    circle_linewidth,
                    x_offset,
                    y_offset,
                    inner_color,
                    outer_color,
                    inner_depth,
                    outer_depth
                )
                i = (i + circle_resolution).toInt()
            }
        }
        run {
            var i = 90
            while (i >= 0) {
                //fourth Quadrant
                circleIndex = initBufferCircleQuadrant(
                    circle_vertices,
                    circleIndex,
                    circle_color,
                    circle_radius,
                    i.toFloat(),
                    -1f,
                    1f,
                    circle_linewidth,
                    x_offset,
                    y_offset,
                    inner_color,
                    outer_color,
                    inner_depth,
                    outer_depth
                )
                i = (i - circle_resolution).toInt()
            }
        }
        run {
            var i = 0
            while (i < 90) {
                //third Quadrant
                circleIndex = initBufferCircleQuadrant(
                    circle_vertices,
                    circleIndex,
                    circle_color,
                    circle_radius,
                    i.toFloat(),
                    -1f,
                    -1f,
                    circle_linewidth,
                    x_offset,
                    y_offset,
                    inner_color,
                    outer_color,
                    inner_depth,
                    outer_depth
                )
                i = (i + circle_resolution).toInt()
            }
        }
        var i = 90
        while (i >= 0) {
            //second Quadrant
            circleIndex = initBufferCircleQuadrant(
                circle_vertices,
                circleIndex,
                circle_color,
                circle_radius,
                i.toFloat(),
                1f,
                -1f,
                circle_linewidth,
                x_offset,
                y_offset,
                inner_color,
                outer_color,
                inner_depth,
                outer_depth
            )
            i = (i - circle_resolution).toInt()
        }
    }

    private fun init_circle_buffer() { //create the buffer for drawing a circle
        init_circle_vertices()
        vertexBuffer = ByteBuffer.allocateDirect(circle_vertices.size * 4).run {
            // use the device hardware's native byte order
            order(ByteOrder.nativeOrder())
            // create a floating point buffer from the ByteBuffer
            asFloatBuffer().apply {
                // add the coordinates to the FloatBuffer
                put(circle_vertices)
                // set the buffer to read the first coordinate
                position(0)
            }
        }
        vertexCount = circle_vertices.size / COORDS_PER_VERTEX
        colorBuffer = ByteBuffer.allocateDirect(circle_color.size * 4).run {
            // use the device hardware's native byte order
            order(ByteOrder.nativeOrder())
            // create a floating point buffer from the ByteBuffer
            asFloatBuffer().apply {
                // add the coordinates to the FloatBuffer
                put(circle_color)
                // set the buffer to read the first coordinate
                position(0)
            }
        }
    }

    /**
     * Sets up the drawing object data for use in an OpenGL ES context.
     */
    init{
        // initialize vertex byte buffer for shape coordinates
        init_circle_buffer()
        // prepare shaders and OpenGL program
        val vertexShader = MyRenderer.loadShader(
            GLES32.GL_VERTEX_SHADER, vertexShaderCode
        )
        val fragmentShader = MyRenderer.loadShader(
            GLES32.GL_FRAGMENT_SHADER, fragmentShaderCode
        )

        mProgram = GLES32.glCreateProgram() // create empty OpenGL Program
        GLES32.glAttachShader(mProgram, vertexShader) // add the vertex shader to program
        GLES32.glAttachShader(mProgram, fragmentShader) // add the fragment shader to program
        GLES32.glLinkProgram(mProgram) // create OpenGL program executables

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES32.glGetAttribLocation(mProgram, "vPosition")
        // Enable a handle to the circle vertices
        GLES32.glEnableVertexAttribArray(mPositionHandle)
        mColorHandle = GLES32.glGetAttribLocation(mProgram, "aVertexColor")
        // Enable a handle to the circle color
        GLES32.glEnableVertexAttribArray(mColorHandle)
        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES32.glGetUniformLocation(mProgram, "uMVPMatrix")
    }

    /**
     * Encapsulates the OpenGL ES instructions for drawing this shape.
     *
     * @param mvpMatrix - The Model View Project matrix in which to draw
     * this shape.
     */
    fun draw(mvpMatrix: FloatArray?) {
        // Add program to OpenGL environment
        GLES32.glUseProgram(mProgram)
        // Apply the projection and view transformation
        GLES32.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0)
        MyRenderer.checkGlError("glUniformMatrix4fv")
        //set the attribute of the vertex to point to the vertex buffer
        GLES32.glVertexAttribPointer(
            mPositionHandle, COORDS_PER_VERTEX,
            GLES32.GL_FLOAT, false,
            vertexStride, vertexBuffer
        )
        //set the attribute of the vertex to point to the vertex buffer
        //set the attibute of the color to point to the color buffer
        GLES32.glVertexAttribPointer(
            mColorHandle, COLOR_PER_VERTEX, GLES32.GL_FLOAT, false, ColorStride, colorBuffer
        )
        // Draw the triangle
        GLES32.glDrawArrays(GLES32.GL_TRIANGLES, 0, vertexCount)
    }

    companion object{
        const val COORDS_PER_VERTEX: Int = 3

    }
}