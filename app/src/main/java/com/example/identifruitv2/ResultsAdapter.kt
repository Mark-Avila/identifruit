package com.example.identifruitv2

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.roundToInt

class ResultsAdapter(
    private val results: MutableList<ResultModel>,
    private val itemClickListener: FruitClickListener,
    private val appContext: Context
): RecyclerView.Adapter<ResultsAdapter.ResultsViewHolder>() {

    class ResultsViewHolder(view: View, clickListener: FruitClickListener): RecyclerView.ViewHolder(view) {
        val labelView: TextView
        val confidenceView: TextView
        val progressView: ProgressBar
        val cardContainer: CardView
        val cardImage: ImageView

        init {
            labelView = view.findViewById(R.id.cardLabel)
            confidenceView = view.findViewById(R.id.cardConfidence)
            progressView = view.findViewById(R.id.cardPercentage)
            cardContainer = view.findViewById(R.id.cardContainer)
            cardImage = view.findViewById(R.id.fruitCardImage)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultsViewHolder {
        return ResultsViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.fruit_card,
                parent,
                false,
            ),
            itemClickListener
        )
    }

    override fun onBindViewHolder(holder: ResultsViewHolder, position: Int) {
        val currResult = results[position]
        val currImageBitmap: Bitmap = getImageFromIndex(currResult.index)

        holder.apply {
            labelView.text = currResult.label
            confidenceView.text = concatPercent(roundNum(currResult.confidence))
            progressView.max = 100
            progressView.progress = (currResult.confidence * 100).roundToInt()
            cardImage.setImageBitmap(currImageBitmap)
            cardContainer.setOnClickListener {
                itemClickListener.onClick(currResult.index)
            }
        }
    }

    override fun getItemCount(): Int {
        return results.size
    }

    private fun concatPercent(num: String): String {
        return "$num %"
    }

    private fun getImageFromIndex(index: Int): Bitmap {
        val currFruit = fruits.getFruitFromIndex(index)
        val imageStream = appContext.assets.open(currFruit?.image ?: "no_image.png")
        return BitmapFactory.decodeStream(imageStream)
    }

    private val fruits = FruitList(appContext)

    private fun roundNum(num: Double): String {
        val toHundreds = num * 100
        val number3digits: Double = String.format("%.3f", toHundreds).toDouble()
        val solution: Double = String.format("%.2f", number3digits).toDouble()

        return solution.toBigDecimal().toPlainString()
    }

    fun addResult(result: ResultModel) {
        results.add(result)
    }
}