package com.jfdedit3.mediagallerynova

import android.Manifest
import android.app.RecoverableSecurityException
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.tabs.TabLayout
import com.jfdedit3.mediagallerynova.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: MediaAdapter
    private lateinit var repository: MediaStoreRepository
    private lateinit var settings: AppSettings

    private var allItems: List<MediaItemModel> = emptyList()
    private var currentTab: MediaTab = MediaTab.ALL
    private var currentQuery: String = ""
    private var selectionMode = false

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.all { it }
        if (granted) loadMedia() else showPermissionDeniedState()
    }

    private val deleteLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            Toast.makeText(this, getString(R.string.deleted_success), Toast.LENGTH_SHORT).show()
            exitSelectionMode()
            loadMedia()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        settings = AppSettings(this)
        settings.applyTheme()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = MediaStoreRepository(this)
        setupToolbar()
        setupRecyclerView()
        setupTabs()
        setupSearch()
        setupRefresh()
        setupSelectionBar()
        checkPermissionsAndLoad()
    }

    override fun onResume() {
        super.onResume()
        applyCurrentSettings()
        if (hasAllPermissions()) loadMedia()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.app_name)
        binding.settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun setupRecyclerView() {
        adapter = MediaAdapter(
            settings = settings,
            onClick = { item ->
                if (selectionMode) {
                    updateSelectionUi()
                } else {
                    startActivity(
                        Intent(this, ViewerActivity::class.java)
                            .putExtra(ViewerActivity.EXTRA_URI, item.uri.toString())
                            .putExtra(ViewerActivity.EXTRA_NAME, item.name)
                            .putExtra(ViewerActivity.EXTRA_TYPE, item.type.name)
                    )
                }
            },
            onLongClick = { item ->
                if (!selectionMode) enterSelectionMode()
                adapter.toggleSelection(item)
                updateSelectionUi()
            }
        )
        binding.recyclerView.adapter = adapter
        applyCurrentSettings()
    }

    private fun applyCurrentSettings() {
        settings.applyTheme()
        binding.recyclerView.layoutManager = GridLayoutManager(this, settings.gridColumns)
        adapter.notifyDataSetChanged()
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
        binding.swipeRefresh.setOnRefreshListener { loadMedia() }
    }

    private fun setupSelectionBar() {
        binding.selectAllButton.setOnClickListener {
            val visible = getFilteredItems()
            enterSelectionMode()
            visible.forEach { if (!adapter.isSelected(it)) adapter.toggleSelection(it) }
            updateSelectionUi()
        }
        binding.deleteSelectedButton.setOnClickListener { deleteSelectedMedia() }
        binding.cancelSelectionButton.setOnClickListener { exitSelectionMode() }
    }

    private fun checkPermissionsAndLoad() {
        if (hasAllPermissions()) loadMedia() else permissionLauncher.launch(requiredPermissions())
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

    private fun getFilteredItems(): List<MediaItemModel> {
        val query = currentQuery.trim().lowercase()
        return allItems.filter { item ->
            val tabMatches = when (currentTab) {
                MediaTab.ALL -> true
                MediaTab.IMAGE -> item.type == MediaType.IMAGE
                MediaTab.VIDEO -> item.type == MediaType.VIDEO
                MediaTab.AUDIO -> item.type == MediaType.AUDIO
            }
            val queryMatches = query.isBlank() || item.name.lowercase().contains(query)
            tabMatches && queryMatches
        }
    }

    private fun applyFilters() {
        val filtered = getFilteredItems()
        adapter.submitList(filtered)
        binding.emptyView.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        if (selectionMode) updateSelectionUi()
    }

    private fun enterSelectionMode() {
        selectionMode = true
        adapter.setSelectionMode(true)
        binding.selectionBar.visibility = View.VISIBLE
    }

    private fun exitSelectionMode() {
        selectionMode = false
        adapter.setSelectionMode(false)
        binding.selectionBar.visibility = View.GONE
        binding.selectionCount.text = getString(R.string.selection_count, 0)
    }

    private fun updateSelectionUi() {
        binding.selectionCount.text = getString(R.string.selection_count, adapter.getSelectedCount())
    }

    private fun deleteSelectedMedia() {
        val selected = adapter.getSelectedItems()
        if (selected.isEmpty()) {
            Toast.makeText(this, getString(R.string.nothing_selected), Toast.LENGTH_SHORT).show()
            return
        }

        val uris = selected.map { it.uri }

        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val intentSender = MediaStore.createDeleteRequest(contentResolver, uris).intentSender
                deleteLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
            } else {
                var deletedCount = 0
                uris.forEach { uri -> deletedCount += contentResolver.delete(uri, null, null) }
                if (deletedCount > 0) {
                    Toast.makeText(this, getString(R.string.deleted_success), Toast.LENGTH_SHORT).show()
                    exitSelectionMode()
                    loadMedia()
                } else {
                    Toast.makeText(this, getString(R.string.delete_failed), Toast.LENGTH_SHORT).show()
                }
            }
        }.recoverCatching { error ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && error is RecoverableSecurityException) {
                deleteLauncher.launch(IntentSenderRequest.Builder(error.userAction.actionIntent.intentSender).build())
            } else {
                throw error
            }
        }.onFailure {
            Toast.makeText(this, it.message ?: getString(R.string.delete_failed), Toast.LENGTH_LONG).show()
        }
    }

    private fun showPermissionDeniedState() {
        binding.progressBar.visibility = View.GONE
        binding.swipeRefresh.isRefreshing = false
        binding.emptyView.visibility = View.VISIBLE
        binding.emptyView.text = getString(R.string.permission_denied_message)
    }
}
