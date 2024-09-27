package com.example.opengles

import android.opengl.GLES20

class Utils {

    //Shaders contain OpenGL Shading Language (GLSL) code that must be compiled prior to
    // using it in the OpenGL ES environment, this method does that
    companion object{
        fun loadShader(type: Int, shaderCode: String): Int {
            // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
            // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
            return GLES20.glCreateShader(type).also { shader ->

                // add the source code to the shader and compile it
                GLES20.glShaderSource(shader, shaderCode)
                GLES20.glCompileShader(shader)
            }
        }
    }
}