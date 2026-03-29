package com.jfdedit3.mediagalleryelite

import android.Manifest
import android.app.AlertDialog
import android.app.RecoverableSecurityException
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.tabs.TabLayout
import com.jfdedit3.mediagalleryelite.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: MediaAdapter
    private lateinit var repository: MediaStoreRepository
    private lateinit var settings: AppSettings
    private lateinit var favoritesStore: FavoritesStore

    private var allItems: List<MediaItemModel> = emptyList()
    private var currentTab: MediaTab = MediaTab.ALL
    private var currentQuery: String = ""
    private var selectionMode = false
    private var currentAudioItem: MediaItemModel? = null

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions.values.all { it }) loadMedia() else showPermissionDeniedState()
    }

    private val deleteLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
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
        favoritesStore = FavoritesStore(this)
        setupToolbar()
        setupRecyclerView()
        setupTabs()
        setupSearch()
        setupRefresh()
        setupSelectionBar()
        setupMiniPlayer()
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
        binding.settingsButton.setOnClickListener { startActivity(Intent(this, SettingsActivity::class.java)) }
        binding.sortButton.setOnClickListener { showSortMenu() }
    }

    private fun setupRecyclerView() {
        adapter = MediaAdapter(settings, favoritesStore,
            onClick = { item ->
                if (selectionMode) {
                    updateSelectionUi()
                } else {
                    openItem(item)
                }
            },
            onLongClick = { item ->
                if (!selectionMode) enterSelectionMode()
                adapter.toggleSelection(item)
                updateSelectionUi()
            },
            onFavoriteClick = { item ->
                favoritesStore.toggleFavorite(item.id)
                applyFilters()
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
                    4 -> MediaTab.FAVORITES
                    5 -> MediaTab.FOLDERS
                    6 -> MediaTab.ALBUMS
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
                currentQuery = query.orEmpty(); applyFilters(); return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                currentQuery = newText.orEmpty(); applyFilters(); return true
            }
        })
    }

    private fun setupRefresh() { binding.swipeRefresh.setOnRefreshListener { loadMedia() } }

    private fun setupSelectionBar() {
        binding.selectAllButton.setOnClickListener {
            val visible = getFilteredItems()
            enterSelectionMode()
            visible.forEach { if (!adapter.isSelected(it)) adapter.toggleSelection(it) }
            updateSelectionUi()
        }
        binding.deleteSelectedButton.setOnClickListener { deleteSelectedMedia() }
        binding.shareSelectedButton.setOnClickListener { shareSelectedMedia() }
        binding.renameButton.setOnClickListener { renameSelectedMedia() }
        binding.cancelSelectionButton.setOnClickListener { exitSelectionMode() }
    }

    private fun setupMiniPlayer() {
        binding.miniPlayerClose.setOnClickListener {
            currentAudioItem = null
            binding.miniPlayer.visibility = View.GONE
        }
    }

    private fun checkPermissionsAndLoad() {
        if (hasAllPermissions()) loadMedia() else permissionLauncher.launch(requiredPermissions())
    }

    private fun hasAllPermissions(): Boolean = requiredPermissions().all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }

    private fun requiredPermissions(): Array<String> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO, Manifest.permission.READ_MEDIA_AUDIO)
    } else arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)

    private fun loadMedia() {
        binding.progressBar.visibility = View.VISIBLE
        binding.emptyView.visibility = View.GONE
        allItems = runCatching { repository.loadMedia() }
            .onFailure { Toast.makeText(this, it.message ?: getString(R.string.error_loading_media), Toast.LENGTH_LONG).show() }
            .getOrDefault(emptyList())
        binding.progressBar.visibility = View.GONE
        binding.swipeRefresh.isRefreshing = false
        applyFilters()
    }

    private fun getFilteredItems(): List<MediaItemModel> {
        val query = currentQuery.trim().lowercase()
        var filtered = allItems.filter { item ->
            val tabMatches = when (currentTab) {
                MediaTab.ALL -> true
                MediaTab.IMAGE -> item.type == MediaType.IMAGE
                MediaTab.VIDEO -> item.type == MediaType.VIDEO
                MediaTab.AUDIO -> item.type == MediaType.AUDIO
                MediaTab.FAVORITES -> favoritesStore.isFavorite(item.id)
                MediaTab.FOLDERS -> true
                MediaTab.ALBUMS -> true
            }
            val valueToMatch = when (currentTab) {
                MediaTab.FOLDERS -> item.folderName
                MediaTab.ALBUMS -> item.albumName
                else -> item.name
            }.lowercase()
            tabMatches && (query.isBlank() || valueToMatch.contains(query))
        }

        filtered = when (currentTab) {
            MediaTab.FOLDERS -> filtered.distinctBy { it.folderName }
            MediaTab.ALBUMS -> filtered.distinctBy { it.albumName }
            else -> filtered
        }

        return when (settings.sortMode) {
            SortMode.DATE_DESC -> filtered.sortedByDescending { it.dateAddedSeconds }
            SortMode.DATE_ASC -> filtered.sortedBy { it.dateAddedSeconds }
            SortMode.NAME_ASC -> filtered.sortedBy { it.name.lowercase() }
            SortMode.NAME_DESC -> filtered.sortedByDescending { it.name.lowercase() }
            SortMode.SIZE_DESC -> filtered.sortedByDescending { it.sizeBytes }
            SortMode.TYPE_ASC -> filtered.sortedBy { it.type.name }
        }
    }

    private fun applyFilters() {
        val filtered = getFilteredItems()
        adapter.submitList(filtered)
        binding.emptyView.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        if (selectionMode) updateSelectionUi()
    }

    private fun openItem(item: MediaItemModel) {
        if (currentTab == MediaTab.FOLDERS) {
            currentQuery = item.folderName
            currentTab = MediaTab.ALL
            binding.tabLayout.getTabAt(0)?.select()
            applyFilters()
            return
        }
        if (currentTab == MediaTab.ALBUMS) {
            currentQuery = item.albumName
            currentTab = MediaTab.ALL
            binding.tabLayout.getTabAt(0)?.select()
            applyFilters()
            return
        }
        if (item.type == MediaType.AUDIO) {
            currentAudioItem = item
            binding.miniPlayer.visibility = View.VISIBLE
            binding.miniPlayerTitle.text = item.name
        }
        startActivity(Intent(this, ViewerActivity::class.java)
            .putExtra(ViewerActivity.EXTRA_URI, item.uri.toString())
            .putExtra(ViewerActivity.EXTRA_NAME, item.name)
            .putExtra(ViewerActivity.EXTRA_TYPE, item.type.name)
            .putExtra(ViewerActivity.EXTRA_AUTOPLAY, settings.autoPlayMedia)
        )
    }

    private fun showSortMenu() {
        val popup = PopupMenu(this, binding.sortButton)
        popup.menu.add(0, 1, 0, getString(R.string.sort_date_desc))
        popup.menu.add(0, 2, 0, getString(R.string.sort_date_asc))
        popup.menu.add(0, 3, 0, getString(R.string.sort_name_asc))
        popup.menu.add(0, 4, 0, getString(R.string.sort_name_desc))
        popup.menu.add(0, 5, 0, getString(R.string.sort_size_desc))
        popup.menu.add(0, 6, 0, getString(R.string.sort_type_asc))
        popup.setOnMenuItemClickListener {
            settings.sortMode = when (it.itemId) {
                1 -> SortMode.DATE_DESC
                2 -> SortMode.DATE_ASC
                3 -> SortMode.NAME_ASC
                4 -> SortMode.NAME_DESC
                5 -> SortMode.SIZE_DESC
                else -> SortMode.TYPE_ASC
            }
            applyFilters()
            true
        }
        popup.show()
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

    private fun updateSelectionUi() { binding.selectionCount.text = getString(R.string.selection_count, adapter.getSelectedCount()) }

    private fun shareSelectedMedia() {
        val selected = adapter.getSelectedItems()
        if (selected.isEmpty()) return
        val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "*/*"
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(selected.map { it.uri }))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_media)))
    }

    private fun renameSelectedMedia() {
        val selected = adapter.getSelectedItems()
        if (selected.size != 1) {
            Toast.makeText(this, getString(R.string.rename_single_only), Toast.LENGTH_SHORT).show()
            return
        }
        val item = selected.first()
        val input = EditText(this)
        input.setText(item.name)
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.rename))
            .setView(input)
            .setPositiveButton(getString(R.string.save)) { _, _ ->
                val values = android.content.ContentValues().apply { put(MediaStore.MediaColumns.DISPLAY_NAME, input.text.toString()) }
                runCatching { contentResolver.update(item.uri, values, null, null) }
                    .onSuccess { loadMedia() }
                    .onFailure { Toast.makeText(this, it.message ?: getString(R.string.rename_failed), Toast.LENGTH_LONG).show() }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun deleteSelectedMedia() {
        val selected = adapter.getSelectedItems()
        if (selected.isEmpty()) { Toast.makeText(this, getString(R.string.nothing_selected), Toast.LENGTH_SHORT).show(); return }
        val uris = selected.map { it.uri }
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val intentSender = MediaStore.createDeleteRequest(contentResolver, uris).intentSender
                deleteLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
            } else {
                var deletedCount = 0
                uris.forEach { deletedCount += contentResolver.delete(it, null, null) }
                if (deletedCount > 0) { Toast.makeText(this, getString(R.string.deleted_success), Toast.LENGTH_SHORT).show(); exitSelectionMode(); loadMedia() }
            }
        }.recoverCatching { error ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && error is RecoverableSecurityException) {
                deleteLauncher.launch(IntentSenderRequest.Builder(error.userAction.actionIntent.intentSender).build())
            } else throw error
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
