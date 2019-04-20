package com.ahcjapps.battlebuddy.stats.scanner

import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import com.ahcjapps.battlebuddy.R
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import org.jetbrains.anko.toast
import java.io.File

class ScannedResults : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanned_results)

        if (intent.hasExtra("path")) {
            loadModel(intent.extras.getString("path"))
        }
    }

    private fun loadModel(path: String) {
        Log.d("IMAGE", path)
        val image = FirebaseVisionImage.fromFilePath(this, Uri.fromFile(File(path)))
        val recognizer = FirebaseVision.getInstance().cloudTextRecognizer
        recognizer.processImage(image).addOnSuccessListener {
            val blocks = it.textBlocks
            if (blocks.size == 0) {
                toast("No text found.")
                return@addOnSuccessListener
            }

            for (block in blocks) {
                val blockText = block.text
                for (line in block.lines) {
                    val lineText = line.text
                    Log.d("IMAGE", lineText)
                    for (element in line.elements) {
                        val elementText = element.text
                    }
                }
            }

            //Log.d("IMAGE", blocks.toString())
        }.addOnFailureListener {
            Log.d("IMAGE", "error", it)
        }
    }
}
