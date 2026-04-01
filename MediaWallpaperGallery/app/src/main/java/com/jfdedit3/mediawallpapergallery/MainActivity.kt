package com.jfdedit3.mediawallpapergallery

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.jfdedit3.mediawallpapergallery.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: MediaAdapter
    private lateinit var repository: MediaRepository

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions.values.all { it }) {
            loadMedia()
        } else {
            Toast.makeText(this, getString(R.string.permission_required), Toast.LENGTH_LONG).show()
            binding.progressBar.visibility = View.GONE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = MediaRepository(this)
        adapter = MediaAdapter { item ->
            startActivity(
                Intent(this, ViewerActivity::class.java)
                    .putExtra(ViewerActivity.EXTRA_URI, item.uri.toString())
                    .putExtra(ViewerActivity.EXTRA_NAME, item.name)
                    .putExtra(ViewerActivity.EXTRA_TYPE, item.type.name)
            )
        }

        binding.toolbar.title = getString(R.string.app_name)
        binding.recyclerView.layoutManager = GridLayoutManager(this, 3)
        binding.recyclerView.adapter = adapter
        binding.swipeRefresh.setOnRefreshListener { loadMedia() }

        if (hasPermissions()) {
            loadMedia()
        } else {
            permissionLauncher.launch(requiredPermissions())
        }
    }

    private fun hasPermissions(): Boolean {
        return requiredPermissions().all { permission ->
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requiredPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private fun loadMedia() {
        binding.progressBar.visibility = View.VISIBLE
        Thread {
            val items = runCatching { repository.loadMedia() }.getOrElse { emptyList() }
            runOnUiThread {
                binding.progressBar.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false
                adapter.submitList(items)
                binding.emptyView.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
            }
        }.start()
    }
}
