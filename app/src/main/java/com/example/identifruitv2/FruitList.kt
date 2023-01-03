package com.example.identifruitv2

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.gson.Gson
import java.io.InputStream

class FruitList(private val context: Context) {
    private val jsonString: String =
        context.assets.open("fruits.json").bufferedReader().use { it.readText() }
    private val jsonData: List<FruitModel> =
        Gson().fromJson(jsonString, Array<FruitModel>::class.java).toList()

    fun getFruitFromIndex(index: Int): FruitModel? {
        return jsonData.find {
            it.index == index
        }
    }

    fun getImageFromIndex(index: Int): Bitmap {
        val currFruit = getFruitFromIndex(index)
        val imageStream = context.assets.open(currFruit?.image ?: "no_image.png")
        return BitmapFactory.decodeStream(imageStream)
    }
}