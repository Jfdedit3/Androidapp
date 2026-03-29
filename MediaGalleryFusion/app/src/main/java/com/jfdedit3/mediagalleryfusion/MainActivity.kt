package com.jfdedit3.mediagalleryfusion

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.tabs.TabLayout
import com.jfdedit3.mediagalleryfusion.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var settings: AppSettings
    private lateinit var state: LibraryState
    private lateinit var repository: MediaStoreRepository
    private lateinit var adapter: MediaAdapter

    private var allItems: List<MediaItemModel> = emptyList()
    private var currentTab: MediaTab = MediaTab.ALL
    private var currentQuery = ""
    private var currentFilter = QuickFilter.NONE
    private var selectedFolder = "All folders"
    private var selectionMode = false

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        if (result.values.all { it }) loadMedia() else showPermissionDeniedState()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        settings = AppSettings(this)
        settings.applyTheme()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        state = LibraryState(this)
        repository = MediaStoreRepository(this)

        setupToolbar()
        setupRecyclerView()
        setupTabs()
        setupSearch()
        setupControls()
        checkPermissionsAndLoad()
    }

    override fun onResume() {
        super.onResume()
        settings.applyTheme()
        applyRecyclerLayout()
        if (hasAllPermissions()) loadMedia()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.app_name)
        binding.settingsButton.setOnClickListener { startActivity(Intent(this, SettingsActivity::class.java)) }
    }

    private fun setupRecyclerView() {
        adapter = MediaAdapter(settings, state, onClick = { item ->
            if (selectionMode) updateSelectionUi() else {
                startActivity(Intent(this, ViewerActivity::class.java)
                    .putExtra(ViewerActivity.EXTRA_URI, item.uri.toString())
                    .putExtra(ViewerActivity.EXTRA_NAME, item.name)
                    .putExtra(ViewerActivity.EXTRA_TYPE, item.type.name))
            }
        }, onLongClick = { item ->
            if (!selectionMode) enterSelectionMode()
            adapter.toggleSelection(item)
            updateSelectionUi()
        })
        binding.recyclerView.adapter = adapter
        applyRecyclerLayout()
    }

    private fun applyRecyclerLayout() {
        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val span = if (settings.viewMode == ViewMode.LIST) 1 else if (isLandscape) settings.landscapeColumns else settings.portraitColumns
        binding.recyclerView.layoutManager = GridLayoutManager(this, span)
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
                applyFiltersAndSorting()
            }
            override fun onTabUnselected(tab: TabLayout.Tab) = Unit
            override fun onTabReselected(tab: TabLayout.Tab) = Unit
        })
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                currentQuery = query.orEmpty()
                state.pushSearch(currentQuery)
                applyFiltersAndSorting()
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                currentQuery = newText.orEmpty()
                applyFiltersAndSorting()
                return true
            }
        })
    }

    private fun setupControls() {
        binding.swipeRefresh.setOnRefreshListener { loadMedia() }

        binding.filterChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            currentFilter = when (checkedIds.firstOrNull()) {
                binding.chipFavorites.id -> QuickFilter.FAVORITES
                binding.chipRecents.id -> QuickFilter.RECENTS
                binding.chipLarge.id -> QuickFilter.LARGE_FILES
                binding.chipScreenshots.id -> QuickFilter.SCREENSHOTS
                binding.chipDownloads.id -> QuickFilter.DOWNLOADS
                binding.chipTrash.id -> QuickFilter.TRASH
                else -> QuickFilter.NONE
            }
            applyFiltersAndSorting()
        }

        binding.sortModeSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, SortMode.values().map { it.name })
        binding.viewModeSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, ViewMode.values().map { it.name })

        binding.sortModeSpinner.setSelection(settings.sortMode.ordinal)
        binding.viewModeSpinner.setSelection(settings.viewMode.ordinal)

        binding.sortApplyButton.setOnClickListener {
            settings.sortMode = SortMode.values()[binding.sortModeSpinner.selectedItemPosition]
            settings.viewMode = ViewMode.values()[binding.viewModeSpinner.selectedItemPosition]
            settings.sortOrder = if (binding.orderSwitch.isChecked) SortOrder.ASC else SortOrder.DESC
            applyRecyclerLayout()
            applyFiltersAndSorting()
        }

        binding.selectionSelectAll.setOnClickListener {
            enterSelectionMode()
            getFilteredItems().forEach { if (!adapter.isSelected(it)) adapter.toggleSelection(it) }
            updateSelectionUi()
        }
        binding.selectionClear.setOnClickListener { exitSelectionMode() }
        binding.selectionFavorite.setOnClickListener {
            adapter.getSelectedItems().forEach { state.toggleFavorite(it.id) }
            exitSelectionMode()
            applyFiltersAndSorting()
        }
        binding.selectionTrash.setOnClickListener {
            state.addToTrash(adapter.getSelectedItems().map { it.id })
            exitSelectionMode()
            applyFiltersAndSorting()
        }
        binding.selectionShare.setOnClickListener {
            val uris = ArrayList(adapter.getSelectedItems().map { it.uri })
            if (uris.isEmpty()) return@setOnClickListener
            startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = "*/*"
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }, getString(R.string.share_selected)))
        }
    }

    private fun checkPermissionsAndLoad() {
        if (hasAllPermissions()) loadMedia() else permissionLauncher.launch(requiredPermissions())
    }

    private fun hasAllPermissions() = requiredPermissions().all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }

    private fun requiredPermissions(): Array<String> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO, Manifest.permission.READ_MEDIA_AUDIO)
    } else arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)

    private fun loadMedia() {
        binding.progressBar.visibility = View.VISIBLE
        allItems = repository.loadMedia()
        binding.progressBar.visibility = View.GONE
        binding.swipeRefresh.isRefreshing = false
        updateFolderSpinner()
        applyFiltersAndSorting()
    }

    private fun updateFolderSpinner() {
        val folders = listOf("All folders") + allItems.map { it.bucketName }.distinct().sorted()
        binding.folderSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, folders)
        binding.folderSpinner.setSelection(folders.indexOf(selectedFolder).takeIf { it >= 0 } ?: 0)
        binding.folderSpinner.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedFolder = folders[position]
                applyFiltersAndSorting()
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) = Unit
        })
    }

    private fun getFilteredItems(): List<MediaItemModel> {
        val q = currentQuery.trim().lowercase()
        val now = System.currentTimeMillis() / 1000
        return allItems.filter { item ->
            val tabOk = when (currentTab) {
                MediaTab.ALL -> true
                MediaTab.IMAGE -> item.type == MediaType.IMAGE
                MediaTab.VIDEO -> item.type == MediaType.VIDEO
                MediaTab.AUDIO -> item.type == MediaType.AUDIO
            }
            val folderOk = selectedFolder == "All folders" || item.bucketName == selectedFolder
            val queryOk = q.isBlank() || item.name.lowercase().contains(q) || item.bucketName.lowercase().contains(q)
            val filterOk = when (currentFilter) {
                QuickFilter.NONE -> !state.isHidden(item.id) && !state.isTrashed(item.id)
                QuickFilter.FAVORITES -> state.isFavorite(item.id) && !state.isTrashed(item.id)
                QuickFilter.RECENTS -> now - item.dateAddedSeconds <= 7 * 24 * 3600 && !state.isTrashed(item.id)
                QuickFilter.LARGE_FILES -> item.sizeBytes >= 100L * 1024L * 1024L && !state.isTrashed(item.id)
                QuickFilter.SCREENSHOTS -> item.bucketName.contains("screenshot", true) && !state.isTrashed(item.id)
                QuickFilter.DOWNLOADS -> item.relativePath.contains("download", true) && !state.isTrashed(item.id)
                QuickFilter.TRASH -> state.isTrashed(item.id)
                QuickFilter.HIDDEN -> state.isHidden(item.id)
            }
            tabOk && folderOk && queryOk && filterOk
        }
    }

    private fun applyFiltersAndSorting() {
        val sorted = getFilteredItems().sortedWith { a, b ->
            val base = when (settings.sortMode) {
                SortMode.NAME -> a.name.compareTo(b.name, true)
                SortMode.DATE -> a.dateAddedSeconds.compareTo(b.dateAddedSeconds)
                SortMode.SIZE -> a.sizeBytes.compareTo(b.sizeBytes)
                SortMode.DURATION -> a.durationMs.compareTo(b.durationMs)
                SortMode.TYPE -> a.type.name.compareTo(b.type.name)
            }
            if (settings.sortOrder == SortOrder.ASC) base else -base
        }
        adapter.submitList(sorted)
        binding.emptyView.visibility = if (sorted.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun enterSelectionMode() {
        selectionMode = true
        binding.selectionBar.visibility = View.VISIBLE
        adapter.setSelectionMode(true)
    }

    private fun exitSelectionMode() {
        selectionMode = false
        binding.selectionBar.visibility = View.GONE
        adapter.setSelectionMode(false)
        updateSelectionUi()
    }

    private fun updateSelectionUi() {
        binding.selectionCount.text = getString(R.string.selection_count, adapter.getSelectedCount())
    }

    private fun showPermissionDeniedState() {
        binding.emptyView.visibility = View.VISIBLE
        binding.emptyView.text = getString(R.string.permission_denied_message)
    }
}
