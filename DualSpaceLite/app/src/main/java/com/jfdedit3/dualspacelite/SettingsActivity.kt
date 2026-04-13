package com.jfdedit3.dualspacelite

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jfdedit3.dualspacelite.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var pinManager: PinManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pinManager = PinManager(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.settings)
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.resetPinButton.setOnClickListener {
            pinManager.clearPin()
            finish()
        }
    }
}
