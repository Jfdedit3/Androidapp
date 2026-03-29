package com.jfdedit3.mediagalleryfusion

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.jfdedit3.mediagalleryfusion.databinding.ActivitySettingsBinding

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

        binding.themeSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listOf("auto", "dark", "light"))
        binding.accentSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listOf("blue", "purple", "green", "orange"))
        binding.themeSpinner.setSelection(listOf("auto", "dark", "light").indexOf(settings.themeMode))
        binding.accentSpinner.setSelection(listOf("blue", "purple", "green", "orange").indexOf(settings.accentColor))
        binding.portraitSlider.value = settings.portraitColumns.toFloat()
        binding.landscapeSlider.value = settings.landscapeColumns.toFloat()
        binding.portraitValue.text = settings.portraitColumns.toString()
        binding.landscapeValue.text = settings.landscapeColumns.toString()
        binding.autoPlaySwitch.isChecked = settings.autoPlayMedia
        binding.fileNamesSwitch.isChecked = settings.showFileNames
        binding.animationsSwitch.isChecked = settings.enableAnimations
        binding.confirmDeleteSwitch.isChecked = settings.confirmDeletion

        binding.saveButton.setOnClickListener {
            settings.themeMode = binding.themeSpinner.selectedItem.toString()
            settings.accentColor = binding.accentSpinner.selectedItem.toString()
            settings.portraitColumns = binding.portraitSlider.value.toInt()
            settings.landscapeColumns = binding.landscapeSlider.value.toInt()
            settings.autoPlayMedia = binding.autoPlaySwitch.isChecked
            settings.showFileNames = binding.fileNamesSwitch.isChecked
            settings.enableAnimations = binding.animationsSwitch.isChecked
            settings.confirmDeletion = binding.confirmDeleteSwitch.isChecked
            recreate()
            finish()
        }

        binding.portraitSlider.addOnChangeListener { _, value, _ -> binding.portraitValue.text = value.toInt().toString() }
        binding.landscapeSlider.addOnChangeListener { _, value, _ -> binding.landscapeValue.text = value.toInt().toString() }
    }
}
