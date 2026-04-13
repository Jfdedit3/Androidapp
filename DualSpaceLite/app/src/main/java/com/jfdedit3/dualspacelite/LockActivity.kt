package com.jfdedit3.dualspacelite

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.jfdedit3.dualspacelite.databinding.ActivityLockBinding

class LockActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLockBinding
    private lateinit var pinManager: PinManager
    private var setupMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLockBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pinManager = PinManager(this)
        setupMode = intent.getBooleanExtra(EXTRA_SETUP_MODE, false)

        binding.titleText.text = if (setupMode) getString(R.string.setup_pin) else getString(R.string.enter_pin)
        binding.unlockButton.text = if (setupMode) getString(R.string.save_pin) else getString(R.string.unlock)

        binding.unlockButton.setOnClickListener {
            val pin = binding.pinInput.text?.toString().orEmpty().trim()
            if (pin.length < 4) {
                Toast.makeText(this, getString(R.string.pin_too_short), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (setupMode) {
                pinManager.savePin(pin)
                setResult(RESULT_OK)
                finish()
            } else {
                if (pinManager.verifyPin(pin)) {
                    setResult(RESULT_OK)
                    finish()
                } else {
                    Toast.makeText(this, getString(R.string.invalid_pin), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        const val EXTRA_SETUP_MODE = "extra_setup_mode"
    }
}
