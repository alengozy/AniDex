package com.example.anidex

import RecommendationAdapter
import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.PersistableBundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.GridLayoutManager
import com.example.anidex.databinding.ActivityRecommendationBinding
import com.example.anidex.databinding.LoadingdialoglayoutBinding
import com.example.anidex.model.AnimeDetail
import com.example.anidex.model.Characters
import com.example.anidex.network.APIService
import com.example.anidex.network.AuthServiceConfig
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager
import com.google.firebase.ml.custom.FirebaseCustomRemoteModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableSource
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import org.json.JSONException
import org.tensorflow.lite.Interpreter
import java.io.InputStream
import java.time.OffsetDateTime
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap

class RecommendationActivity : AppCompatActivity() {
    private var userId = 1
    private val remoteModel = FirebaseCustomRemoteModel.Builder("anime_recsys").build()
    private val conditions = FirebaseModelDownloadConditions.Builder()
        .requireWifi()
        .build()
    private lateinit var interpreter: Interpreter
    private var idList = mutableListOf<Int>()
    private var userList = mutableListOf<Int>()
    private lateinit var recIds: ArrayList<Int>
    private val service = APIService.createJikanClient()
    private val results = ArrayList<AnimeDetail>()
    private lateinit var loadingDialog: Dialog
    private val loading = MutableLiveData<Boolean>()
    private var url = ""
    private var authServiceConfig: AuthServiceConfig = AuthServiceConfig()
    private var authState: AuthState = AuthState(authServiceConfig.getServiceConfig())
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private val malClient: APIService = APIService.createMALClient()
    private lateinit var binding: ActivityRecommendationBinding
    private lateinit var dialogBinding: LoadingdialoglayoutBinding
    private lateinit var database: DatabaseReference

    @ExperimentalStdlibApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Firebase.database.setPersistenceEnabled(true)
        binding = ActivityRecommendationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.recToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.recToolbar.setNavigationOnClickListener {
            finish()
        }
        loading.postValue(true)
        dialogBinding = LoadingdialoglayoutBinding.inflate(layoutInflater)
        when {
            savedInstanceState!=null -> {
                try {
                    savedInstanceState
                        .getString("authState", "{}")
                        .let { AuthState.jsonDeserialize(it) }
                } catch (ex: JSONException) {
                    val m = Throwable().stackTrace[0]
                    Log.e("RecommendationActivity", "${m}: $ex")
                    authState = AuthState()
                }
            }
            intent.getStringExtra("authState")!=null -> {
                authState= AuthState.jsonDeserialize(intent.getStringExtra("authState")!!)
            }
            else -> authState = AuthState()
        }
        initViews()
        val authResp = AuthorizationResponse.fromIntent(intent)
        val authEx = AuthorizationException.fromIntent(intent)
        if(authResp!=null){
            authState.update(authResp, authEx)
            val tokenReq = authResp.createTokenExchangeRequest()
            AuthorizationService(this)
                .performTokenRequest(tokenReq) { resp2, ex2 ->
                    authState.update(resp2, ex2)
                    if (resp2 != null) {
                        authState.update(resp2, ex2)
                        onTokenResponse()
                    } else {
                       return@performTokenRequest
                    }
                }
        } else {
            onTokenResponse()
        }
    }

    @ExperimentalStdlibApi
    private fun onTokenResponse(){
        authState.performActionWithFreshTokens(AuthorizationService(this), object :
            AuthState.AuthStateAction {
            override fun execute(
                accessToken: String?,
                idToken: String?,
                ex: AuthorizationException?
            ) {
                if (ex != null) {
                    // negotiation for fresh tokens failed, check ex for more details
                    return
                }
                compositeDisposable.add(
                    malClient.getLoggedUserId("Bearer $accessToken")
                        ?.observeOn(AndroidSchedulers.mainThread())
                        ?.subscribeOn(Schedulers.io())
                        ?.subscribe({ resp ->
                            userId = resp.id
                            onIdFetched()
                        }, { t ->
                            onError(t)
                            loading.postValue(false)
                        })

                )
            }
        })



    }


    @ExperimentalStdlibApi
    private fun onIdFetched(){
        FirebaseModelManager.getInstance().download(remoteModel, conditions)
            .addOnCompleteListener {
                // Download complete. Depending on your app, you could enable the ML
                // feature, or switch from the local model to the remote model, etc.
                database = Firebase.database.reference
                val inputStream: InputStream = assets.open("users.txt")
                inputStream.bufferedReader().forEachLine { userList.add(it.toInt()) }
                val mapListener = object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        idList = dataSnapshot.child("idList").getValue<MutableList<Int>>()!!
                        getRecommendations(userId)
                    }


                    override fun onCancelled(databaseError: DatabaseError) {
                    }
                }
                database.addListenerForSingleValueEvent(mapListener)

            }
    }

    @ExperimentalStdlibApi
    private fun getRecommendations(id: Int) {
        val dbId = userList.indexOf(id)
        val user = FloatArray(1) { dbId.toFloat() }
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
        var delay: Long = 0
        var dbId: Int
        for(i in recIds){
            Handler().postDelayed({
                dbId = this.idList[i].toInt()
                url = "https://api.jikan.moe/v3/anime/$dbId"
                compositeDisposable.add(
                    service.getAnimeDetail(url)
                        ?.observeOn(AndroidSchedulers.mainThread())
                        ?.subscribeOn(Schedulers.io())
                        ?.subscribe({ response ->
                            if (response != null) {
                                results.add(response)
                                updateAdapter()
                            }
                        }, { t ->
                            onError(t)
                        })
                )

            }, delay)
            delay+=500


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
        compositeDisposable.add(
            service.getCharactersDetail(url)
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
        loading.postValue(false)
        startActivity(intent)
    }

    override fun onSaveInstanceState(
        outState: Bundle,
        outPersistentState: PersistableBundle
    ) {
        super.onSaveInstanceState(outState, outPersistentState)
        outState.putString("authState", authState.jsonSerializeString())
        writeAuthState(authState)
    }

    private fun initViews(){
        binding.recrecycler.layoutManager =
            if (this.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                GridLayoutManager(this@RecommendationActivity, 2)
            } else {

                GridLayoutManager(this@RecommendationActivity, 4)
            }
        //dialogBinding.loadingcardtext.text = "Getting Recommendations. Please Wait..."
        initDialog()
    }
    @ExperimentalStdlibApi
    private fun updateAdapter(){
        binding.recrecycler.adapter = RecommendationAdapter(
            itemOnClick,
            results,
            this@RecommendationActivity
        )
        if(results.size==10)
            loading.postValue(false)
    }
    private fun onError(t: Throwable){

    }

    @ExperimentalStdlibApi
    private val itemOnClick: (View, Int, Int) -> Unit = { _, position, _ ->
        val listItem = results.get(position)
        Toast.makeText(this@RecommendationActivity, listItem.englishtitle, Toast.LENGTH_SHORT).show()
        //dialogBinding.loadingcardtext.text = "Getting Title Details. Please Wait..."
        loading.postValue(true)
        val detailIntent = Intent(this, DetailsActivity::class.java)
        Handler().postDelayed({
            clicked(listItem, detailIntent)
        }, 300)
    }

    private fun initDialog() {
        loadingDialog = Dialog(this@RecommendationActivity)
        loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        loadingDialog.setCancelable(false)
        loadingDialog.setContentView(dialogBinding.root)
        loading.observe(this@RecommendationActivity, {
            if (loading.value!!)
                loadingDialog.show()
            else
                loadingDialog.dismiss()

        })

    }



    private fun writeAuthState(state: AuthState) {
        val authPrefs: SharedPreferences = getSharedPreferences("auth", MODE_PRIVATE)
        authPrefs.edit()
            .putString("stateJson", state.jsonSerializeString())
            .apply()
    }

    override fun finish(){
        writeAuthState(authState)
        super.finish()
    }
    }



