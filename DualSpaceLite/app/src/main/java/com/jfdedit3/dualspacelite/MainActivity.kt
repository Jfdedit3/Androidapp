package com.jfdedit3.dualspacelite

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.jfdedit3.dualspacelite.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var pinManager: PinManager
    private lateinit var registry: AppRegistry
    private lateinit var adapter: AppTileAdapter

    private val addAppsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        loadPinnedApps()
    }

    private val unlockLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode != RESULT_OK) finish() else loadPinnedApps()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pinManager = PinManager(this)
        registry = AppRegistry(this)

        setupUi()
        ensureUnlocked()
    }

    override fun onResume() {
        super.onResume()
        loadPinnedApps()
    }

    private fun setupUi() {
        adapter = AppTileAdapter(
            onClick = { app ->
                val launched = registry.launch(app.packageName)
                if (!launched) Toast.makeText(this, getString(R.string.unable_to_launch), Toast.LENGTH_SHORT).show()
            },
            onLongClick = { app ->
                val pinned = registry.pinnedPackages().toMutableSet()
                pinned.remove(app.packageName)
                registry.setPinnedPackages(pinned)
                loadPinnedApps()
            }
        )

        binding.appsRecycler.layoutManager = GridLayoutManager(this, 3)
        binding.appsRecycler.adapter = adapter

        binding.addAppsButton.setOnClickListener {
            addAppsLauncher.launch(Intent(this, AddAppActivity::class.java))
        }
        binding.vaultButton.setOnClickListener {
            startActivity(Intent(this, VaultActivity::class.java))
        }
        binding.settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun ensureUnlocked() {
        if (!pinManager.hasPin()) {
            unlockLauncher.launch(Intent(this, LockActivity::class.java).putExtra(LockActivity.EXTRA_SETUP_MODE, true))
        } else {
            unlockLauncher.launch(Intent(this, LockActivity::class.java).putExtra(LockActivity.EXTRA_SETUP_MODE, false))
        }
    }

    private fun loadPinnedApps() {
        val apps = registry.loadPinnedApps()
        adapter.submitList(apps)
        binding.emptyText.text = if (apps.isEmpty()) getString(R.string.no_pinned_apps) else ""
    }
}
