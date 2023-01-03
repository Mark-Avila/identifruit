package com.example.identifruitv2

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class IdentifyActivity : AppCompatActivity(), FruitClickListener {

    private lateinit var resultsList: RecyclerView
    private lateinit var imageView: ImageView
    private lateinit var results: MutableList<ResultModel>
    private lateinit var identifyLabel: TextView
    private lateinit var unidLayout: LinearLayout
    private var image: String? = null
    private var imageBitmap: Bitmap? = null
    private var flag = false

    override fun onClick(index: Int) {
        val intent = Intent(this, FruitActivity::class.java)
        intent.putExtra("fruit_index", index)
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_identify)

        val resultsAdapter = ResultsAdapter(mutableListOf(), this, applicationContext)

        identifyLabel = findViewById(R.id.identifyLabel)
        resultsList = findViewById(R.id.fruitResultsList)
        unidLayout = findViewById(R.id.unid_container)
        imageView = findViewById(R.id.fruitImage)

        resultsList.visibility = View.VISIBLE
        identifyLabel.visibility = View.VISIBLE
        unidLayout.visibility = View.GONE

        resultsList.adapter = resultsAdapter
        resultsList.layoutManager = LinearLayoutManager(this)

        results = intent.getParcelableArrayListExtra<ResultModel>("results") as ArrayList<ResultModel>
        image = intent.getStringExtra("image")
        imageBitmap = intent.getParcelableExtra("imageBitmap")

        if (image != null) {
            imageView.setImageURI(Uri.parse(image))
        } else if (imageBitmap != null) {
            imageView.setImageBitmap(imageBitmap)
        }

        for (item in results) {
            if (item.confidence >= 0.20) {
                flag = true
            }
        }

        if (flag) {
            resultsAdapter.addResult(results[0])
            resultsAdapter.addResult(results[1])
            resultsAdapter.addResult(results[2])
            resultsAdapter.addResult(results[3])
            resultsAdapter.addResult(results[4])
        } else {
            resultsList.visibility = View.GONE
            identifyLabel.visibility = View.GONE
            unidLayout.visibility = View.VISIBLE
        }




    }


}