package com.jfdedit3.mediagalleryultra.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.jfdedit3.mediagalleryultra.MediaStoreRepository
import com.jfdedit3.mediagalleryultra.ui.adapter.FeedAdapter

class TikTokFeedActivity : AppCompatActivity() {

    private lateinit var pager: ViewPager2
    private lateinit var repo: MediaStoreRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

        pager = ViewPager2(this)
        pager.orientation = ViewPager2.ORIENTATION_VERTICAL
        setContentView(pager)

        repo = MediaStoreRepository(this)

        val media = repo.loadMedia()

        pager.adapter = FeedAdapter(media)

        pager.offscreenPageLimit = 1
    }
}
