package com.jfdedit3.robloxuserfinder

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.jfdedit3.robloxuserfinder.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: UserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = UserAdapter()
        binding.resultsRecycler.layoutManager = LinearLayoutManager(this)
        binding.resultsRecycler.adapter = adapter

        binding.searchButton.setOnClickListener {
            performSearch()
        }
    }

    private fun performSearch() {
        val query = binding.searchInput.text?.toString().orEmpty().trim()
        if (query.isBlank()) {
            Toast.makeText(this, getString(R.string.enter_username), Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.emptyText.visibility = View.GONE

        lifecycleScope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) {
                    RobloxApi.searchUsers(query)
                }
            }

            binding.progressBar.visibility = View.GONE

            result.onSuccess { users ->
                adapter.submitList(users)
                binding.emptyText.visibility = if (users.isEmpty()) View.VISIBLE else View.GONE
                if (users.isEmpty()) binding.emptyText.text = getString(R.string.no_results)
            }.onFailure {
                adapter.submitList(emptyList())
                binding.emptyText.visibility = View.VISIBLE
                binding.emptyText.text = getString(R.string.search_failed)
            }
        }
    }
}
