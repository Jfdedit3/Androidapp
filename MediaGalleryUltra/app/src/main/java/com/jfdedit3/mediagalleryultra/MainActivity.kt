package com.jfdedit3.mediagalleryultra

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jfdedit3.mediagalleryultra.ui.TikTokFeedActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // IMPORTANT :
        // On ne charge plus d’UI ici.
        // MainActivity devient uniquement un launcher.

        val intent = Intent(this, TikTokFeedActivity::class.java)
        startActivity(intent)

        // kill MainActivity pour éviter retour arrière inutile
        finish()
    }
}
