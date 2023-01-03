package com.example.identifruitv2

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView

class FruitActivity : AppCompatActivity() {
    private lateinit var fruitImage: ImageView
    private lateinit var fruitTagalog: TextView
    private lateinit var fruitEnglish: TextView
    private lateinit var fruitDesc: TextView
    private lateinit var fruits: FruitList

    private fun getImageFromIndex(index: Int): Bitmap {
        val currFruit = fruits.getFruitFromIndex(index)
        val imageStream = applicationContext.assets.open(currFruit?.image ?: "no_image.png")
        return BitmapFactory.decodeStream(imageStream)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fruit)
        fruits = FruitList(applicationContext)

        val fruits = FruitList(applicationContext)

        fruitImage = findViewById(R.id.fruitActImage)
        fruitTagalog = findViewById(R.id.fruitActTagalog)
        fruitEnglish = findViewById(R.id.fruitActEnglish)
        fruitDesc = findViewById(R.id.fruitActDesc)

        val fruitIndex: Int = intent.getIntExtra("fruit_index", 0)

        val fruitInfo: FruitModel? = fruits.getFruitFromIndex(fruitIndex)

        if (fruitInfo != null) {
            fruitTagalog.text = fruitInfo.labels.tg
            fruitEnglish.text = fruitInfo.labels.en
            fruitDesc.text = fruitInfo.desc

            val imageBitmap: Bitmap = fruits.getImageFromIndex(fruitIndex)

            fruitImage.setImageBitmap(imageBitmap)
        }
    }
}