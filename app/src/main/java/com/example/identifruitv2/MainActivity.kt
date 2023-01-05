package com.example.identifruitv2

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.identifruitv2.ml.G1DaFruitsclassification
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var cameraButton: Button
    private lateinit var galleryButton: Button
    private val imageSize: Int = 128
    private lateinit var currentPhotoPath: String

    class TFResultModel(
        val index: Int,
        val confidence: Float,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cameraButton = findViewById(R.id.cameraBtn)
        cameraButton.setOnClickListener(this)

        galleryButton = findViewById(R.id.galleryBtn)
        galleryButton.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.cameraBtn -> {
                if (
                    ContextCompat.checkSelfPermission(
                        this@MainActivity,
                        android.Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    val photoFile: File? = try {
                        createImageFile()
                    } catch (ex: java.lang.Exception) {
                        ex.printStackTrace()
                        null
                    }

                    if (photoFile != null) {
                        val photoUri: Uri = FileProvider.getUriForFile(
                            this,
                            "com.example.android.fileprovider",
                            photoFile
                        )

                        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                        startActivityForResult(cameraIntent, 3)
                    }




                } else {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(android.Manifest.permission.CAMERA),
                        100
                    )
                }
            }

            R.id.galleryBtn -> {
                val galleryIntent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
                startActivityForResult(galleryIntent, 1)
            }
        }
    }


    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            if (requestCode == 3) {

                val imageUri: Uri = Uri.fromFile(File(currentPhotoPath))
                var imageBitmap: Bitmap? = null

                try {
                    imageBitmap = if (Build.VERSION.SDK_INT < 28) {
                        MediaStore.Images.Media.getBitmap(
                            this.contentResolver,
                            imageUri
                        )
                    } else {
                        val source = ImageDecoder.createSource(this.contentResolver, imageUri)
                        ImageDecoder.decodeBitmap(source).copy(Bitmap.Config.ARGB_8888, true)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                if (imageBitmap != null) {
                    val scaledImage: Bitmap =
                        Bitmap.createScaledBitmap(imageBitmap, imageSize, imageSize, false)

                    val newResults: MutableList<ResultModel> = classifyImage(scaledImage)
                    //
                    val identifyIntent = Intent(this, IdentifyActivity::class.java)
                    identifyIntent.putParcelableArrayListExtra("results", ArrayList(newResults))
                    identifyIntent.putExtra("image", imageUri.toString())

                    startActivity(identifyIntent)
                }

            } else if (requestCode == 1) {

                if (data != null) {

                    val dat: Uri = data.data!!
                    var newImage: Bitmap? = null

                    try {
                        newImage = if (Build.VERSION.SDK_INT < 28) {
                            MediaStore.Images.Media.getBitmap(
                                this.contentResolver,
                                dat
                            )
                        } else {
                            val source = ImageDecoder.createSource(this.contentResolver, dat)
                            ImageDecoder.decodeBitmap(source).copy(Bitmap.Config.ARGB_8888, true)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    if (newImage != null) {
                        val scaledImage: Bitmap =
                            Bitmap.createScaledBitmap(newImage, imageSize, imageSize, false)

//                        val passedImage: Bitmap =
//                            Bitmap.createScaledBitmap(newImage, 256, 256, false)

                        val newResults: MutableList<ResultModel> = classifyImage(scaledImage)
                        //
                        val identifyIntent = Intent(this, IdentifyActivity::class.java)
                        identifyIntent.putParcelableArrayListExtra("results", ArrayList(newResults))
                        identifyIntent.putExtra("image", data.data.toString())

                        startActivity(identifyIntent)
                    }

                }

            }
        }
    }

    private fun classifyImage(thisImage: Bitmap): MutableList<ResultModel> {
        val model = G1DaFruitsclassification.newInstance(applicationContext)

        // Creates inputs for reference.
        val inputFeature0 =
            TensorBuffer.createFixedSize(intArrayOf(1, 128, 128, 3), DataType.FLOAT32)
        val byteBuffer: ByteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3)
        byteBuffer.order(ByteOrder.nativeOrder())

        val intValues = IntArray(imageSize * imageSize)
        thisImage.getPixels(intValues, 0, thisImage.width, 0, 0, thisImage.width, thisImage.height)

        var pixel = 0

        for (i in 1..imageSize) {
            for (j in 1..imageSize) {
                val value: Int = intValues[pixel++]
                byteBuffer.putFloat(((value shr 16) and 0xFF) * (1f / 255))
                byteBuffer.putFloat(((value shr 8) and 0xFF) * (1f / 255))
                byteBuffer.putFloat((value and 0xFF) * (1f / 255))
            }
        }

        inputFeature0.loadBuffer(byteBuffer)

        // Runs model inference and gets result.
        val outputs = model.process(inputFeature0)
        val outputFeature0 = outputs.outputFeature0AsTensorBuffer

        val confidences: FloatArray = outputFeature0.floatArray

        val tfResults: MutableList<TFResultModel> = ArrayList()
        for ((confIdx, conf) in confidences.withIndex()) {
            tfResults.add(TFResultModel(confIdx, conf))
        }

        tfResults.sortByDescending { it.confidence }
        val labels = ResultsLabels()

        val results: MutableList<ResultModel> = ArrayList()

        val fruits = FruitList(applicationContext)

        for (resIdx in 0..4) {
            val currIndex = tfResults[resIdx].index + 1
            val fruitItem = fruits.getFruitFromIndex(currIndex)

            results.add(
                ResultModel(
                    tfResults[resIdx].index + 1,
                    fruitItem?.labels?.tg,
                    tfResults[resIdx].confidence.toDouble(),
                )
            )
        }

        // Releases model resources
        model.close()

        return results
    }


}