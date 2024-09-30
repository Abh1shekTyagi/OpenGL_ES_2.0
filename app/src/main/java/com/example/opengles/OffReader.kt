package com.example.opengles
import android.util.Log
import java.io.InputStream
import java.util.Scanner

data class Vertex(var x: Float = 0f, var y: Float = 0f, var z: Float = 0f)

data class Polygon(var noSides: Int = 0, var v: IntArray = intArrayOf())

data class OffModel(
    var vertices: Array<Vertex> = arrayOf(),
    var polygons: Array<Polygon> = arrayOf(),
    var numberOfVertices: Int = 0,
    var numberOfPolygons: Int = 0
)
class OffReader {
    private val model = OffModel()
    fun reader(inputFile: InputStream): FloatArray {
        val model = readOffFile(inputFile)

//        Log.d("klsndflksdnflksdnf","OFF")
        Log.d("klsndflksdnflksdnf", "${model.numberOfVertices} ${model.numberOfPolygons} 0")

        for (i in 0 until model.numberOfVertices) {
            println("${model.vertices[i].x} ${model.vertices[i].y} ${model.vertices[i].z}")
        }

        for (i in 0 until model.numberOfPolygons) {
            print("${model.polygons[i].noSides} ")
            for (j in 0 until model.polygons[i].noSides) {
                print("${model.polygons[i].v[j]} ")
            }
            println()
        }
        return model.vertices.flatMap { listOf(it.x, it.y, it.z) }.toFloatArray()
    }

    private fun readOffFile(offFile: InputStream): OffModel {
        val input = Scanner(offFile)

        val type = input.next()

        if (!type!!.contentEquals("OFF")) {
//            Log.d("klsndflksdnflksdnf","Not an OFF file")
            throw IllegalArgumentException("Invalid OFF file")
        }

        model.numberOfVertices = input.nextInt()
        model.numberOfPolygons = input.nextInt()
        val noEdges = input.nextInt()

        model.vertices = Array(model.numberOfVertices) { Vertex() }
        model.polygons = Array(model.numberOfPolygons) { Polygon() }

        // Read the vertices' location
        for (i in 0 until model.numberOfVertices) {
            model.vertices[i].x = input.nextFloat()
            model.vertices[i].y = input.nextFloat()
            model.vertices[i].z = input.nextFloat()
        }

        // Read the polygons
        for (i in 0 until model.numberOfPolygons) {
            val n = input.nextInt()
            model.polygons[i].noSides = n
            model.polygons[i].v = IntArray(n)

            for (j in 0 until n) {
                model.polygons[i].v[j] = input.nextInt()
            }
        }

        input.close()
        return model
    }
}