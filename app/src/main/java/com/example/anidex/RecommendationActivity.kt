package com.example.anidex

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager
import com.google.firebase.ml.custom.FirebaseCustomRemoteModel
import org.tensorflow.lite.Interpreter

class RecommendationActivity : AppCompatActivity() {
    private val remoteModel = FirebaseCustomRemoteModel.Builder("anime_recsys").build()
    private val conditions = FirebaseModelDownloadConditions.Builder()
        .requireWifi()
        .build()
    private lateinit var interpreter: Interpreter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reccomendation)
        FirebaseModelManager.getInstance().download(remoteModel, conditions)
            .addOnCompleteListener {
                // Download complete. Depending on your app, you could enable the ML
                // feature, or switch from the local model to the remote model, etc.
            }
        val remoteModel = FirebaseCustomRemoteModel.Builder("anime_recsys").build()
        FirebaseModelManager.getInstance().getLatestModelFile(remoteModel)
            .addOnCompleteListener { task ->
                val modelFile = task.result
                if (modelFile != null) {
                    interpreter = Interpreter(modelFile)
                    val user = FloatArray(1) {109490f}
                    val animes = FloatArray(1){1f}
                    val input = arrayOf(user, animes)
                    val output = Array(1){FloatArray(1)}
                    val outputs: MutableMap<Int, Any> = HashMap()
                    outputs[0] = output
                    interpreter.runForMultipleInputsOutputs(input, outputs)
                    print(output)
                }


            }

    }


}