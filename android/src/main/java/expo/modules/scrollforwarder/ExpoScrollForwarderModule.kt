package expo.modules.scrollforwarder

import com.facebook.react.views.scroll.ReactScrollView
import com.facebook.react.views.scroll.ReactScrollViewManager
import expo.modules.core.Module
import expo.modules.core.ModuleDefinition
import expo.modules.core.ViewManagerDefinition

class ExpoScrollForwarderModule : Module() {
    override fun definition(): ModuleDefinition {
        Name("ExpoScrollForwarder")

        View(ExpoScrollForwarderView::class.java) {
            Prop("scrollViewTag") { view: ExpoScrollForwarderView, prop: Int? ->
                view.scrollViewTag = prop
            }
        }
    }
}
