package com.example.anidex

import RecommendationAdapter
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.anidex.databinding.ActivityRecommendationBinding
import com.example.anidex.model.AnimeDetail
import com.example.anidex.model.Characters
import com.example.anidex.network.APIService
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager
import com.google.firebase.ml.custom.FirebaseCustomRemoteModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import org.tensorflow.lite.Interpreter
import java.io.InputStream
import java.util.*
import kotlin.collections.HashMap
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.time.OffsetDateTime

class RecommendationActivity : AppCompatActivity() {
    private val remoteModel = FirebaseCustomRemoteModel.Builder("anime_recsys").build()
    private val conditions = FirebaseModelDownloadConditions.Builder()
        .requireWifi()
        .build()
    private lateinit var interpreter: Interpreter
    private var idList = mutableListOf<Int>()
    private var userList = mutableListOf<Int>()
    private lateinit var recIds: ArrayList<Int>
    private val service = APIService.createClient()
    private val results = ArrayList<AnimeDetail>()
    private lateinit var loadingDialog: Dialog
    private val loading = MutableLiveData<Boolean>()
    private var url = ""
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private lateinit var binding: ActivityRecommendationBinding
    @ExperimentalStdlibApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecommendationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loading.postValue(true)
        initViews()
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
    @ExperimentalStdlibApi
    private fun getRecommendations(id: Int) {
        val dbId = userList.indexOf(id)
        val user = FloatArray(1) {dbId.toFloat()}
        lateinit var anime: FloatArray
        lateinit var input: Array<FloatArray>
        lateinit var output: Array<FloatArray>
        val ratings: MutableMap<Int, Float> = HashMap()
        val outputs: MutableMap<Int, Any> = HashMap()
        recIds = ArrayList<Int>()
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
                    fetchRecommendations()

                }
            }
    }
    
    //https://stackoverflow.com/questions/52770018/how-to-effectively-get-the-n-lowest-values-from-the-collection-top-n-in-kotlin
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


    @ExperimentalStdlibApi
    private fun fetchRecommendations(){
        var dbId: Int
        for(i in recIds){
            Handler().postDelayed({
            dbId = this.idList[i]
            url = "https://api.jikan.moe/v3/anime/$dbId"
            compositeDisposable.add(service.getAnimeDetail(url)
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribeOn(Schedulers.io())
                ?.subscribe({ response ->
                    if (response != null) {
                        results.add(response)
                        updateAdapter()
                    }
                }, { t ->
                    onError(t)
                }))

            }, i.toLong())
    }
}

    @ExperimentalStdlibApi
    private fun clicked(
        response: AnimeDetail?,
        intent: Intent
    ) {
        val dates = listOf(
            OffsetDateTime.parse(response?.aired?.from),
            if (response?.aired?.to != null) OffsetDateTime.parse(response.aired.to) else null
        )
        var month = dates[0]?.month.toString().toLowerCase(Locale.ROOT).capitalize(Locale.ROOT)
        var day = dates[0]?.dayOfMonth
        var year = dates[0]?.year
        intent.putExtra(
            "fromdate",
            String.format(resources.getString(R.string.datestring), month, day, year)
        )
        intent.putExtra("status", response?.status.toString())
        if (!response?.status.equals("Currently Airing") && !response?.status.equals("Not yet aired")) {
            month = dates[1]?.month.toString().toLowerCase(Locale.ROOT).capitalize(Locale.ROOT)
            day = dates[1]?.dayOfMonth
            year = dates[1]?.year
            intent.putExtra(
                "enddate",
                String.format(resources.getString(R.string.datestring), month, day, year)
            )
        }
        intent.putExtra("malId", response?.malId)
        intent.putExtra("title", response?.title.toString())
        intent.putExtra("image", response?.imageUrl)
        intent.putExtra("score", response?.score.toString())
        intent.putExtra("synopsis", response?.synopsis.toString())
        intent.putExtra("trailerlink", response?.trailer.toString())
        intent.putExtra("episodes", response?.episodes)
        intent.putExtra("englishtitle", response?.englishtitle)
        intent.putExtra("genres", response?.genres)
        intent.putExtra("rank", response?.rank.toString())
        fetchCharacters(response?.malId, intent)
    }

    private fun fetchCharacters(malId: Int?, intent: Intent) {
        url = "https://api.jikan.moe/v3/anime/$malId/characters_staff"
        compositeDisposable.add(service.getCharactersDetail(url)
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribeOn(Schedulers.io())
            ?.subscribe({ response ->
                onCharacterSuccess(response, intent)
            }, { t ->
                onError(t)
            })
        )
    }

    private fun onCharacterSuccess(response: Characters?, intent: Intent) {
        intent.putExtra("characters", response)
        startActivity(intent)
    }

    private fun initViews(){
        binding.recrecycler.layoutManager = LinearLayoutManager(this@RecommendationActivity, LinearLayoutManager.HORIZONTAL, false)
        initDialog()
    }
    @ExperimentalStdlibApi
    private fun updateAdapter(){
        binding.recrecycler.adapter = RecommendationAdapter(itemOnClick, results, this@RecommendationActivity)
        if(results.size==10)
            loading.postValue(false)
    }
    private fun onError(t:Throwable){

    }

    @ExperimentalStdlibApi
    private val itemOnClick: (View, Int, Int) -> Unit = { _, position, _ ->
        val listItem = results.get(position)
        Toast.makeText(this@RecommendationActivity, listItem.englishtitle, Toast.LENGTH_SHORT).show()
        //initDialog()
        val detailIntent = Intent(this, DetailsActivity::class.java)
        Handler().postDelayed({
            clicked(listItem, detailIntent)
        }, 300)
    }

    private fun initDialog() {
        loadingDialog = Dialog(this@RecommendationActivity)
        loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        loadingDialog.setCancelable(false)
        loadingDialog.setContentView(R.layout.loadingdialoglayout)
        loading.observe(this@RecommendationActivity, {
            if(loading.value!!)
                loadingDialog.show()
            else
                loadingDialog.dismiss()

        })

    }
    }



