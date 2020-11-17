package com.example.anidex

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager
import com.google.firebase.ml.custom.FirebaseCustomRemoteModel
import org.tensorflow.lite.Interpreter
import java.io.File
import java.io.InputStream
import java.util.*
import java.util.stream.IntStream
import kotlin.collections.HashMap

class RecommendationActivity : AppCompatActivity() {
    private val remoteModel = FirebaseCustomRemoteModel.Builder("anime_recsys").build()
    private val conditions = FirebaseModelDownloadConditions.Builder()
        .requireWifi()
        .build()
    private lateinit var interpreter: Interpreter
    private var idList = mutableListOf<Int>()
    private var userList = mutableListOf<Int>()
    private lateinit var recIds: Dictionary<Int, Int>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reccomendation)

        FirebaseModelManager.getInstance().download(remoteModel, conditions)
            .addOnCompleteListener {
                // Download complete. Depending on your app, you could enable the ML
                // feature, or switch from the local model to the remote model, etc.
                var inputStream: InputStream = assets.open("ids.txt")
                inputStream.bufferedReader().forEachLine { idList.add(it.toInt()) }
                inputStream.close()
                inputStream = assets.open("users.txt")
                inputStream.bufferedReader().forEachLine { userList.add(it.toInt()) }
                getRecommendations(5301397)
            }
    }
    private fun getRecommendations(id: Int) {
        val dbId = userList.indexOf(id)
        val user = FloatArray(1) {dbId.toFloat()}
        lateinit var anime: FloatArray
        lateinit var input: Array<FloatArray>
        lateinit var output: Array<FloatArray>
        val ratings: MutableMap<Int, Float> = HashMap()
        val outputs: MutableMap<Int, Any> = HashMap()
        val recIds = ArrayList<Int>()
        FirebaseModelManager.getInstance().getLatestModelFile(remoteModel)
            .addOnCompleteListener { task ->
                val modelFile = task.result
                if (modelFile != null) {
                    interpreter = Interpreter(modelFile)
                    for(i in 0..8112){
                        anime = FloatArray(1){i.toFloat()}
                        input = arrayOf(user, anime)
                        output = Array(1){FloatArray(1)}
                        outputs[0] = output
                        interpreter.runForMultipleInputsOutputs(input, outputs)
                        val rat = ((outputs[0] as Array<*>).filterIsInstance<FloatArray>())[0][0]
                        ratings[i] = rat
                    }
                    val topRatings = top(10, ratings.values)
                    for(i in topRatings){
                        recIds.add(ratings.values.indexOf(i))
                    }
                    print(recIds)
                }
            }

    }

    fun <T : Comparable<T>> top(n: Int, collection: Iterable<T>): List<T> {
        return collection.fold(ArrayList<T>()) { topList, candidate ->
            if (topList.size < n || candidate > topList.last()) {
                // ideally insert at the right place
                topList.add(candidate)
                topList.sortDescending()
                // trim to size
                if (topList.size > n)
                    topList.removeAt(n)
            }
            topList
        }
    }

    }

private operator fun Any?.get(i: Int): Float {
    return i.toFloat()
}


