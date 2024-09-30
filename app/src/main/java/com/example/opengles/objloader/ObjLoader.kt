package com.example.opengles.objloader

import android.content.Context
import android.util.Log
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader


class ObjLoader(context: Context, file: String){
    val numFaces: Int

    val normals: FloatArray
    val textureCoordinates: FloatArray
    val positions: FloatArray
    val vertexArray: FloatArray
    val indexArray: IntArray

    init {
        val vertices = mutableListOf<Float>()
        val normals = mutableListOf<Float>()
        val textures = mutableListOf<Float>()
        val faces = mutableListOf<String>()

        var reader: BufferedReader? = null
        val startTime = System.currentTimeMillis()
        try {
            val input = InputStreamReader(
                context.assets.open(
                    file
                )
            )
            reader = BufferedReader(input)

            reader.forEachLine {line-> //this can be done under 1 second try.
                val parts = line.trim().split("\\s+".toRegex()).toTypedArray()
                when (parts[0]) {
                    "v" -> {
                        // vertices
                        vertices.addAll(parts.drop(1).map { it.toFloat()})
                    }

                    "vt" -> {
                        // textures usually z is 0
                        textures.addAll(parts.drop(1).map { it.toFloat()})
                    }

                    "vn" -> {
                        // normals
                        normals.addAll(parts.drop(1).map { it.toFloat()})
                    }

                    "f" -> {
                        // faces: vertex/texture/normal
                        faces.addAll(parts.drop(1))
                    }
                }
            }
        } catch (e: IOException) {
            // cannot load or read file
        } finally {
            if (reader != null) {
                try {
                    reader.close()
                } catch (e: IOException) {
                    //log the exception
                }
            }
        }
        vertexArray = vertices.toFloatArray()
        numFaces = faces.size
        indexArray = IntArray(numFaces)
        this.normals = FloatArray(numFaces * 3)
        textureCoordinates = FloatArray(numFaces * 2)
        positions = FloatArray(numFaces * 3)
        var positionIndex = 0
        var normalIndex = 0
        var textureIndex = 0
        for ((i,face) in faces.withIndex()) {
            val parts = face.split("/".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
            indexArray[i] = parts.first().toInt()
            var index = 3 * (parts[0].toShort() - 1)
            positions[positionIndex++] = vertices[index++]
            positions[positionIndex++] = vertices[index++]
            positions[positionIndex++] = vertices[index]

            index = 2 * (parts[1].toShort() - 1)
            textureCoordinates[normalIndex++] = textures[index++]
            // NOTE: Bitmap gets y-inverted
            textureCoordinates[normalIndex++] = 1 - textures[index]

            index = 3 * (parts[2].toShort() - 1)
            this.normals[textureIndex++] = normals[index++]
            this.normals[textureIndex++] = normals[index++]
            this.normals[textureIndex++] = normals[index]
        }
        Log.d("testing readtime for","$file is ${System.currentTimeMillis() - startTime}")
    }
}