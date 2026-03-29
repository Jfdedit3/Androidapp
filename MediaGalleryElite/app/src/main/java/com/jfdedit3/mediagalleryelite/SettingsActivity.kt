package com.jfdedit3.mediagalleryelite

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jfdedit3.mediagalleryelite.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var settings: AppSettings

    override fun onCreate(savedInstanceState: Bundle?) {
        settings = AppSettings(this)
        settings.applyTheme()
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.settings)
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.darkModeSwitch.isChecked = settings.darkMode
        binding.autoPlaySwitch.isChecked = settings.autoPlayMedia
        binding.fileNamesSwitch.isChecked = settings.showFileNames
        binding.gridColumnsSlider.value = settings.gridColumns.toFloat()
        binding.gridValue.text = settings.gridColumns.toString()

        binding.darkModeSwitch.setOnCheckedChangeListener { _, isChecked -> settings.darkMode = isChecked; recreate() }
        binding.autoPlaySwitch.setOnCheckedChangeListener { _, isChecked -> settings.autoPlayMedia = isChecked }
        binding.fileNamesSwitch.setOnCheckedChangeListener { _, isChecked -> settings.showFileNames = isChecked }
        binding.gridColumnsSlider.addOnChangeListener { _, value, _ ->
            val columns = value.toInt()
            settings.gridColumns = columns
            binding.gridValue.text = columns.toString()
        }
    }
}
