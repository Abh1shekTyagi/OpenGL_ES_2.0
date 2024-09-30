package com.example.opengles

import android.opengl.GLES32
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer
import kotlin.math.cos
import kotlin.math.sin


class PhongSphere {
    private val vertexShaderCode =
        "attribute vec3 aVertexPosition;" + "uniform mat4 uMVPMatrix;varying vec4 vColor;" +
                "attribute vec3 aVertexNormal;" +  //attribute variable for normal vectors
                "attribute vec4 aVertexColor;" +  //attribute variable for vertex colors
                "uniform vec3 uLightSourceLocation;" +  //location of the light source (for diffuse and specular light)
                "uniform vec3 uAmbientColor;" +  //uniform variable for Ambient color
                "varying vec3 vAmbientColor;" +
                "uniform vec4 uDiffuseColor;" +  //color of the diffuse light
                "varying vec4 vDiffuseColor;" +
                "varying float vDiffuseLightWeighting;" +  //diffuse light intensity
                "uniform vec3 uAttenuation;" +  //light attenuation
                "uniform vec4 uSpecularColor;" +
                "varying vec4 vSpecularColor;" +
                "varying float vSpecularLightWeighting; " +
                "uniform float uMaterialShininess;" +  //----------
                "void main() {" +
                "gl_Position = uMVPMatrix *vec4(aVertexPosition,1.0);" +
                "vec4 mvPosition=uMVPMatrix*vec4(aVertexPosition,1.0);" +
                "vec3 lightDirection=normalize(uLightSourceLocation-mvPosition.xyz);" +
                "vec3 transformedNormal = normalize((uMVPMatrix * vec4(aVertexNormal, 0.0)).xyz);" +
                "vAmbientColor=uAmbientColor;" +
                "vDiffuseColor=uDiffuseColor;" +
                "vSpecularColor=uSpecularColor; " +
                "vec3 eyeDirection=normalize(-mvPosition.xyz);" +
                "vec3 reflectionDirection=reflect(-lightDirection,transformedNormal);" +
                "vec3 vertexToLightSource = mvPosition.xyz-uLightSourceLocation;" +
                "float diff_light_dist = length(vertexToLightSource);" +
                "float attenuation = 1.0 / (uAttenuation.x" +
                "                           + uAttenuation.y * diff_light_dist" +
                "                           + uAttenuation.z * diff_light_dist * diff_light_dist);" +
                "vDiffuseLightWeighting =attenuation*max(dot(transformedNormal,lightDirection),0.0);" +
                "vSpecularLightWeighting=attenuation*pow(max(dot(reflectionDirection,eyeDirection), 0.0), uMaterialShininess);" +
                "vColor=aVertexColor;" +
                "}"
    private val fragmentShaderCode = "precision lowp float;varying vec4 vColor; " +
            "varying vec3 vAmbientColor;" +
            "varying vec4 vDiffuseColor;" +
            "varying float vDiffuseLightWeighting;" +
            "varying vec4 vSpecularColor;" +
            "varying float vSpecularLightWeighting; " +
            "void main() {" +
            "vec4 diffuseColor=vDiffuseLightWeighting*vDiffuseColor;" +
            "vec4 specularColor=vSpecularLightWeighting*vSpecularColor;" +
            "gl_FragColor=vec4(vColor.xyz*vAmbientColor,1)+specularColor+diffuseColor;" +
            "}"
    private var vertexBuffer: FloatBuffer? = null
    private var colorBuffer: FloatBuffer? = null
    private var normalBuffer: FloatBuffer? = null
    private var indexBuffer: IntBuffer? = null
    private var mProgram = 0
    private var mPositionHandle = 0
    private var mNormalHandle: Int = 0
    private var mColorHandle:kotlin.Int = 0

    //--------
    private var diffuseColorHandle = 0
    private var mMVPMatrixHandle = 0
    private var lightLocationHandle = 0
    private var uAmbientColorHandle:kotlin.Int = 0
    private var specularColorHandle = 0
    private var materialShininessHandle = 0
    private var attenuateHandle = 0
    //--------
    // number of coordinates per vertex in this array

    val COORDS_PER_VERTEX: Int = 3 //--------
    // number of coordinates per vertex in this array

    val COLOR_PER_VERTEX: Int = 4

    //---------
    private val vertexStride = COORDS_PER_VERTEX * 4 // 4 bytes per vertex
    private val colorStride = COLOR_PER_VERTEX * 4

    private lateinit var SphereVertex: FloatArray

    private lateinit var  SphereColor: FloatArray

    private lateinit var  SphereIndex: IntArray

    private lateinit var  SphereNormal: FloatArray

    private var  lightlocation: FloatArray = FloatArray(3)

    private var  attenuation: FloatArray = FloatArray(3) //light attenuation

    private  var  diffusecolor: FloatArray = FloatArray(4) //diffuse light colour

    private  var  specularcolor: FloatArray = FloatArray(4) //specular highlight colour

    private var MaterialShininess: Float = 10f //material shiness


    //--------
    private fun createShpere(radius: Float, nolatitude: Int, nolongitude: Int) {
        val vertices = FloatArray(65535)
        val normal = FloatArray(65535)
        val pindex = IntArray(65535)
        val pcolor = FloatArray(65535)
        var vertexindex = 0
        var normindex = 0
        var colorindex = 0
        var indx = 0
        val dist = 0f
        for (row in 0..nolatitude) {
            val theta = row * Math.PI / nolatitude
            val sinTheta = sin(theta)
            val cosTheta = cos(theta)
            for (col in 0..nolongitude) {
                val phi = col * 2 * Math.PI / nolongitude
                val sinPhi = sin(phi)
                val cosPhi = cos(phi)
                val x = cosPhi * sinTheta
                val y = cosTheta
                val z = sinPhi * sinTheta
                normal[normindex++] = x.toFloat()
                normal[normindex++] = y.toFloat()
                normal[normindex++] = z.toFloat()
                vertices[vertexindex++] = (radius * x).toFloat()
                vertices[vertexindex++] = (radius * y).toFloat() + dist
                vertices[vertexindex++] = (radius * z).toFloat()
                pcolor[colorindex++] = 1f
                pcolor[colorindex++] = 0f //Math.abs(tcolor);
                pcolor[colorindex++] = 0f
                pcolor[colorindex++] = 1f
                //--------
                val u = (col / nolongitude.toFloat())
                val v = (row / nolatitude.toFloat())
            }
        }
        for (row in 0 until nolatitude) {
            for (col in 0 until nolongitude) {
                val first = (row * (nolongitude + 1)) + col
                val second = first + nolongitude + 1
                pindex[indx++] = first
                pindex[indx++] = second
                pindex[indx++] = first + 1
                pindex[indx++] = second
                pindex[indx++] = second + 1
                pindex[indx++] = first + 1
            }
        }

        SphereVertex = vertices.copyOf(vertexindex)
        SphereIndex = pindex.copyOf(indx)
        SphereNormal = normal.copyOf(normindex)
        SphereColor = pcolor.copyOf(colorindex)
    }

    init {
        createShpere(2f, 30, 30)
        // initialize vertex byte buffer for shape coordinates
        vertexBuffer =
            ByteBuffer.allocateDirect(SphereVertex.size * 4).run {
                order(ByteOrder.nativeOrder())
                asFloatBuffer().apply {
                    put(SphereVertex)
                    position(0)
                }
            } // (# of coordinate values * 4 bytes per float)

        indexBuffer = IntBuffer.allocate(SphereIndex.size).apply {
            put(SphereIndex)
            position(0)
        }

        colorBuffer = ByteBuffer.allocateDirect(SphereColor.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(SphereColor)
                position(0)
            }
        }
        normalBuffer =
            ByteBuffer.allocateDirect(SphereNormal.size * 4).run {
                order(ByteOrder.nativeOrder())
                asFloatBuffer().apply {
                    put(SphereNormal)
                    position(0)
                }
            } // (# of coordinate values * 4 bytes per float)
        ///============
        lightlocation[0] = 2f
        lightlocation[1] = 1f
        lightlocation[2] = 2f
        specularcolor[0] = 1f
        specularcolor[1] = 1f
        specularcolor[2] = 1f
        specularcolor[3] = 1f
        //////////////////////
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
        MyRenderer.checkGlError("glVertexAttribPointer")
        mColorHandle = GLES32.glGetAttribLocation(mProgram, "aVertexColor")
        GLES32.glEnableVertexAttribArray(mColorHandle)
        GLES32.glVertexAttribPointer(
            mColorHandle,
            COLOR_PER_VERTEX,
            GLES32.GL_FLOAT,
            false,
            colorStride,
            colorBuffer
        )

        mNormalHandle = GLES32.glGetAttribLocation(mProgram, "aVertexNormal")
        GLES32.glEnableVertexAttribArray(mNormalHandle)
        GLES32.glVertexAttribPointer(
            mNormalHandle,
            COORDS_PER_VERTEX,
            GLES32.GL_FLOAT,
            false,
            vertexStride,
            normalBuffer
        )
        // MyRenderer.checkGlError("glVertexAttribPointer");
        // get handle to shape's transformation matrix
        //nMatrixHandle=GLES32.glGetUniformLocation(mProgram, "uNMatrix");
        lightLocationHandle = GLES32.glGetUniformLocation(mProgram, "uLightSourceLocation")
        diffuseColorHandle = GLES32.glGetUniformLocation(mProgram, "uDiffuseColor")
        diffusecolor[0] = 1f
        diffusecolor[1] = 1f
        diffusecolor[2] = 1f
        diffusecolor[3] = 1f
        attenuateHandle = GLES32.glGetUniformLocation(mProgram, "uAttenuation")
        attenuation[0] = 1f
        attenuation[1] = 0.14f
        attenuation[2] = 0.07f
        uAmbientColorHandle = GLES32.glGetUniformLocation(mProgram, "uAmbientColor")
        // MyRenderer.checkGlError("uAmbientColor");
        specularColorHandle = GLES32.glGetUniformLocation(mProgram, "uSpecularColor")
        materialShininessHandle = GLES32.glGetUniformLocation(mProgram, "uMaterialShininess")
        mMVPMatrixHandle = GLES32.glGetUniformLocation(mProgram, "uMVPMatrix")
        //MyRenderer.checkGlError("glGetUniformLocation-mMVPMatrixHandle");
    }

    fun draw(mvpMatrix: FloatArray?) {
        GLES32.glUseProgram(mProgram) // Add program to OpenGL environment
        // Apply the projection and view transformation
        GLES32.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0)
        //MyRenderer.checkGlError("glUniformMatrix4fv");
        GLES32.glUniform3fv(lightLocationHandle, 1, lightlocation, 0)
        GLES32.glUniform4fv(diffuseColorHandle, 1, diffusecolor, 0)
        GLES32.glUniform3fv(attenuateHandle, 1, attenuation, 0)
        GLES32.glUniform3f(uAmbientColorHandle, 0.6f, 0.6f, 0.6f)
        GLES32.glUniform4fv(specularColorHandle, 1, specularcolor, 0)
        GLES32.glUniform1f(materialShininessHandle, MaterialShininess)
        //set the attribute of the vertex to point to the vertex buffer
        GLES32.glVertexAttribPointer(
            mPositionHandle, COORDS_PER_VERTEX,
            GLES32.GL_FLOAT, false, vertexStride, vertexBuffer
        )
        GLES32.glVertexAttribPointer(
            mColorHandle, COLOR_PER_VERTEX,
            GLES32.GL_FLOAT, false, colorStride, colorBuffer
        )
        GLES32.glVertexAttribPointer(
            mNormalHandle, COORDS_PER_VERTEX,
            GLES32.GL_FLOAT, false, vertexStride, normalBuffer
        )
        // Draw the sphere
        GLES32.glDrawElements(
            GLES32.GL_TRIANGLES,
            SphereIndex.size,
            GLES32.GL_UNSIGNED_INT,
            indexBuffer
        )
    }

    fun setLightLocation(px: Float, py: Float, pz: Float) {
        lightlocation[0] = px
        lightlocation[1] = py
        lightlocation[2] = pz
    }
}