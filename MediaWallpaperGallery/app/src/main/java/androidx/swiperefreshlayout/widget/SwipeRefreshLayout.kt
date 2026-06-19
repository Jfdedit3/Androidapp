package androidx.swiperefreshlayout.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout

class SwipeRefreshLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private var refreshListener: (() -> Unit)? = null
    var isRefreshing: Boolean = false

    fun setOnRefreshListener(listener: () -> Unit) {
        refreshListener = listener
    }

    fun triggerRefresh() {
        isRefreshing = true
        refreshListener?.invoke()
    }
}
