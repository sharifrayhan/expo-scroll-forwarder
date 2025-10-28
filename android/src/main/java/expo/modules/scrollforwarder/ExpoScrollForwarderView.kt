package expo.modules.scrollforwarder

import android.content.Context
import android.view.View
import android.widget.ScrollView
import androidx.core.widget.NestedScrollView
import expo.modules.core.views.ExpoView

class ExpoScrollForwarderView(context: Context) : ExpoView(context) {

    var scrollViewTag: Int? = null
        set(value) {
            field = value
            tryFindScrollView()
        }

    private var scrollView: NestedScrollView? = null

    private fun tryFindScrollView() {
        scrollViewTag?.let { tag ->
            val parentView = rootView
            val found = parentView.findViewById<View>(tag)
            if (found is NestedScrollView) {
                scrollView = found
            }
        }
    }

    fun scrollToOffset(offset: Int, animated: Boolean = true) {
        scrollView?.let {
            if (animated) {
                it.smoothScrollTo(0, offset)
            } else {
                it.scrollTo(0, offset)
            }
        }
    }
}
