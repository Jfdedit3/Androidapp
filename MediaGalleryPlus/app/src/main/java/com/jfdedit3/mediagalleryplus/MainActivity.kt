package com.jfdedit3.mediagalleryplus

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.tabs.TabLayout
import com.jfdedit3.mediagalleryplus.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: MediaAdapter
    private lateinit var repository: MediaStoreRepository

    private var allItems: List<MediaItemModel> = emptyList()
    private var currentTab: MediaTab = MediaTab.ALL
    private var currentQuery: String = ""

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.all { it }
        if (granted) {
            loadMedia()
        } else {
            showPermissionDeniedState()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = MediaStoreRepository(this)
        setupToolbar()
        setupRecyclerView()
        setupTabs()
        setupSearch()
        setupRefresh()
        checkPermissionsAndLoad()
    }

    override fun onResume() {
        super.onResume()
        if (hasAllPermissions()) {
            loadMedia()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.app_name)
    }

    private fun setupRecyclerView() {
        adapter = MediaAdapter { item ->
            startActivity(
                Intent(this, ViewerActivity::class.java)
                    .putExtra(ViewerActivity.EXTRA_URI, item.uri.toString())
                    .putExtra(ViewerActivity.EXTRA_NAME, item.name)
                    .putExtra(ViewerActivity.EXTRA_TYPE, item.type.name)
            )
        }
        binding.recyclerView.layoutManager = GridLayoutManager(this, 3)
        binding.recyclerView.adapter = adapter
    }

    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                currentTab = when (tab.position) {
                    1 -> MediaTab.IMAGE
                    2 -> MediaTab.VIDEO
                    3 -> MediaTab.AUDIO
                    else -> MediaTab.ALL
                }
                applyFilters()
            }

            override fun onTabUnselected(tab: TabLayout.Tab) = Unit
            override fun onTabReselected(tab: TabLayout.Tab) = Unit
        })
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                currentQuery = query.orEmpty()
                applyFilters()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                currentQuery = newText.orEmpty()
                applyFilters()
                return true
            }
        })
    }

    private fun setupRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            loadMedia()
        }
    }

    private fun checkPermissionsAndLoad() {
        if (hasAllPermissions()) {
            loadMedia()
        } else {
            permissionLauncher.launch(requiredPermissions())
        }
    }

    private fun hasAllPermissions(): Boolean {
        return requiredPermissions().all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requiredPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
            )
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private fun loadMedia() {
        binding.progressBar.visibility = View.VISIBLE
        binding.emptyView.visibility = View.GONE

        allItems = runCatching { repository.loadMedia() }
            .onFailure {
                Toast.makeText(this, it.message ?: getString(R.string.error_loading_media), Toast.LENGTH_LONG).show()
            }
            .getOrDefault(emptyList())

        binding.progressBar.visibility = View.GONE
        binding.swipeRefresh.isRefreshing = false
        applyFilters()
    }

    private fun applyFilters() {
        val query = currentQuery.trim().lowercase()

        val filtered = allItems.filter { item ->
            val tabMatches = when (currentTab) {
                MediaTab.ALL -> true
                MediaTab.IMAGE -> item.type == MediaType.IMAGE
                MediaTab.VIDEO -> item.type == MediaType.VIDEO
                MediaTab.AUDIO -> item.type == MediaType.AUDIO
            }
            val queryMatches = query.isBlank() || item.name.lowercase().contains(query)
            tabMatches && queryMatches
        }

        adapter.submitList(filtered)
        binding.emptyView.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun showPermissionDeniedState() {
        binding.progressBar.visibility = View.GONE
        binding.swipeRefresh.isRefreshing = false
        binding.emptyView.visibility = View.VISIBLE
        binding.emptyView.text = getString(R.string.permission_denied_message)
    }
}
