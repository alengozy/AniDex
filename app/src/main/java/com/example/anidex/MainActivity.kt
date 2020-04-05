package com.example.anidex

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.paging.PagedList
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.example.anidex.model.Anime
import com.example.anidex.model.NetworkState
import com.example.anidex.ui.*
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.drawer_layout.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var  toolbar: Toolbar
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var animeViewModel: AnimeViewModel
    private lateinit var animeadapter: AnimeAdapter
    private var dataList: MutableList<Anime> = mutableListOf()

    private var page : Int = 1
    private var loading: Boolean = false
    private val itemOnClick: (View, Int, Int) -> Unit = { view, position, type ->
        val listItem = animeViewModel.animeList.value?.get(position)
        Toast.makeText(this@MainActivity, listItem?.title.toString(), Toast.LENGTH_SHORT).show()
        val intent = Intent(this, DetailsActivity::class.java)
        startActivity(intent)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        swipeRefreshLayout.setOnRefreshListener {
        }
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
        animeViewModel = ViewModelProvider(this).get(AnimeViewModel::class.java)
        initViews()
        initSwipeRefresh()
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    private fun initViews(){
        animeadapter = AnimeAdapter(itemOnClick)
        rv_anime.apply{
            setHasFixedSize(true)
            itemAnimator = DefaultItemAnimator()
            layoutManager =
                if(this.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    GridLayoutManager(this@MainActivity, 2)
                } else {

                    GridLayoutManager(this@MainActivity, 4)
                }
            if(adapter == null){
                adapter = animeadapter
            }
            setOnClickListener { Toast.makeText(this@MainActivity, animeViewModel.animeList.value?.get(2)?.title.toString(), Toast.LENGTH_SHORT).show()  }
            animeViewModel.animeList.observe(this@MainActivity, Observer<PagedList<Anime>>{animeadapter.submitList(it)})
            animeViewModel.getNetworkState().observe(this@MainActivity, Observer<NetworkState>{animeadapter.setNetworkState(it)})

        }
        animeViewModel.getNetworkState().observe(this@MainActivity, Observer{networkState -> if(networkState?.status != NetworkState.LOADING.status && networkState?.status!=NetworkState.LOADED.status)
            Toast.makeText(this@MainActivity, "${networkState.message}. Swipe-up to try again", Toast.LENGTH_SHORT).show()
            swipeRefreshLayout.isRefreshing = false
        })
    }
    private fun initSwipeRefresh(){
        animeViewModel.getRefreshState().observe(this, Observer{networkState ->

                    swipeRefreshLayout.isRefreshing = networkState?.status == NetworkState.LOADING.status



        })
        swipeRefreshLayout.setOnRefreshListener { animeViewModel.refresh() }
    }

}
