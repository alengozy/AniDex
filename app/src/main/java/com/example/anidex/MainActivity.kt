package com.example.anidex

import android.content.res.Configuration
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.GridLayoutManager
import com.example.anidex.model.Anime
import com.example.anidex.network.APIService
import com.example.anidex.ui.CardViewBinder
import com.example.anidex.ui.FeedAdapter
import com.example.anidex.ui.FeedItemBinder
import com.example.anidex.ui.FeedItemClass
import com.google.android.material.navigation.NavigationView
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.drawer_layout.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var  toolbar: Toolbar
    lateinit var drawerLayout: DrawerLayout
    lateinit var navigationView: NavigationView
    private var compositeDisposable: CompositeDisposable? = null
    private var dataList: ArrayList<Anime>? = null
    private var adapter: FeedAdapter? = null
    private var page : Int = 1
    private var param : String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        compositeDisposable = CompositeDisposable()
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, 0, 0
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        navigationView.setNavigationItemSelectedListener(this)
        initViews()
        loadJSON()
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun initViews(){
        rv_anime.setHasFixedSize(true)
        if (this.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            rv_anime.layoutManager = GridLayoutManager(this, 2)
        } else {
            rv_anime.layoutManager = GridLayoutManager(this, 4)
        }
    }

    fun loadJSON(){
        val requestInterface = APIService.createClient()
        compositeDisposable?.add(
            requestInterface.getSeries(page, param)
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribeOn(Schedulers.io())
                ?.subscribe(this::handleResponse, this::handleError))


    }
    fun cardClick(data: Anime){

    }
    private fun handleResponse(animeList: List<Anime>){
        if(adapter == null) {
            val viewBinders = mutableMapOf<FeedItemClass, FeedItemBinder>()
            val cardViewBinder = CardViewBinder { data: Anime -> cardClick(data) }
            @Suppress("UNCHECKED_CAST")
            viewBinders.put(cardViewBinder.modelClass, cardViewBinder as FeedItemBinder)
            adapter = FeedAdapter(viewBinders)
        }
        rv_anime.apply{
            layoutManager = GridLayoutManager(this@MainActivity, 2)
        }
        if(rv_anime.adapter == null){
            rv_anime.adapter = adapter
        }
        (rv_anime.adapter as FeedAdapter).submitList(animeList ?: emptyList())
    }
    private fun handleError(error: Throwable){
        Toast.makeText(this, "Error ${error.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable?.clear()
    }
}
