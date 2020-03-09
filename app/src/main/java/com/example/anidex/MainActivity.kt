package com.example.anidex

import android.content.res.Configuration
import android.icu.lang.UCharacter
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.anidex.model.Anime
import com.example.anidex.model.Top
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
    private var dataList: MutableList<Anime> = mutableListOf()
    private var adapter: FeedAdapter? = null
    private var page : Int = 1
    private var loading: Boolean = false
    private var totalItems: Int = 0
    private var lastVisibleItem: Int = 0
    private var param : String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        swipeRefreshLayout.setOnRefreshListener {
            refreshData()
        }
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
        fetchPage()

    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    private fun initViews(){




    }

    fun refreshData(){
        page = 1
        dataList.clear()
        loadJSON()
        swipeRefreshLayout.isRefreshing = false
        loading = false
    }
    fun fetchPage(){
        loadJSON()
        page++
    }
    fun loadJSON(){
        val requestInterface = APIService.createClient()
        compositeDisposable?.add(
            requestInterface.getSeries(page)
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribeOn(Schedulers.io())
                ?.subscribe(this::handleResponse, this::handleError))

    }
    fun cardClick(data: Anime){
        Toast.makeText(this, data.title, Toast.LENGTH_SHORT).show()
    }
    private fun handleResponse(animeList: Top){
        animeList.top?.let { dataList.addAll(it) }
        if(adapter == null) {
            val viewBinders = mutableMapOf<FeedItemClass, FeedItemBinder>()
            val cardViewBinder = CardViewBinder (this){ data: Anime -> cardClick(data) }
            @Suppress("UNCHECKED_CAST")
            viewBinders.put(cardViewBinder.modelClass, cardViewBinder as FeedItemBinder)
            adapter = FeedAdapter(viewBinders)
        }
        rv_anime.apply{
            setHasFixedSize(true)
            itemAnimator = DefaultItemAnimator()
            layoutManager =
                if(this.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    GridLayoutManager(this@MainActivity, 2)
                } else {

                    GridLayoutManager(this@MainActivity, 4)
                }
            addOnScrollListener(object: RecyclerView.OnScrollListener(){
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    totalItems = (layoutManager as GridLayoutManager).itemCount
                    lastVisibleItem = (layoutManager as GridLayoutManager).findLastVisibleItemPosition()
                    if(!loading && totalItems<=(lastVisibleItem+3)){
                        loading = true
                        fetchPage()
                    }
                }

            })
        }
        if(rv_anime.adapter == null){
            rv_anime.adapter = adapter
        }
        (rv_anime.adapter as FeedAdapter).submitList(dataList as List<Any>? ?: emptyList())
        loading = false
    }
    private fun handleError(error: Throwable){
        Toast.makeText(this, "Error ${error.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable?.clear()
    }
}
