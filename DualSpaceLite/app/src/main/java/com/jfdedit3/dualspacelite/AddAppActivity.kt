package com.jfdedit3.dualspacelite

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.jfdedit3.dualspacelite.databinding.ActivityAddAppBinding

class AddAppActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddAppBinding
    private lateinit var registry: AppRegistry
    private lateinit var adapter: AppTileAdapter
    private val selected = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddAppBinding.inflate(layoutInflater)
        setContentView(binding.root)

        registry = AppRegistry(this)
        selected.addAll(registry.pinnedPackages())

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.add_apps)
        binding.toolbar.setNavigationOnClickListener { finish() }

        adapter = AppTileAdapter(
            onClick = { app ->
                if (selected.contains(app.packageName)) selected.remove(app.packageName) else selected.add(app.packageName)
                binding.selectionText.text = getString(R.string.selected_count, selected.size)
            }
        )

        binding.appsRecycler.layoutManager = GridLayoutManager(this, 3)
        binding.appsRecycler.adapter = adapter
        adapter.submitList(registry.loadInstalledApps())
        binding.selectionText.text = getString(R.string.selected_count, selected.size)

        binding.saveButton.setOnClickListener {
            registry.setPinnedPackages(selected)
            setResult(RESULT_OK)
            finish()
        }
    }
}
