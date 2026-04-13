package com.jfdedit3.dualspacelite

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.jfdedit3.dualspacelite.databinding.ActivityVaultBinding
import java.io.File

class VaultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVaultBinding
    private lateinit var adapter: VaultFileAdapter

    private val pickFilesLauncher = registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        uris.forEach { importUri(it) }
        loadVaultFiles()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVaultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.private_vault)
        binding.toolbar.setNavigationOnClickListener { finish() }

        adapter = VaultFileAdapter(
            onOpen = { file ->
                val uri = androidx.core.content.FileProvider.getUriForFile(this, "$packageName.provider", file)
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "*/*")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(intent)
            },
            onDelete = { file ->
                file.delete()
                loadVaultFiles()
            }
        )

        binding.vaultRecycler.layoutManager = LinearLayoutManager(this)
        binding.vaultRecycler.adapter = adapter

        binding.importButton.setOnClickListener {
            pickFilesLauncher.launch(arrayOf("image/*", "video/*", "audio/*"))
        }

        loadVaultFiles()
    }

    private fun vaultDir(): File = File(filesDir, "vault").apply { mkdirs() }

    private fun importUri(uri: Uri) {
        val name = contentResolver.getType(uri)?.replace('/', '_') + "_" + System.currentTimeMillis()
        val outFile = File(vaultDir(), name)
        contentResolver.openInputStream(uri)?.use { input ->
            outFile.outputStream().use { output -> input.copyTo(output) }
        }
    }

    private fun loadVaultFiles() {
        val files = vaultDir().listFiles()?.sortedByDescending { it.lastModified() }?.toList().orEmpty()
        adapter.submitList(files)
        binding.emptyText.text = if (files.isEmpty()) getString(R.string.vault_empty) else ""
    }
}
