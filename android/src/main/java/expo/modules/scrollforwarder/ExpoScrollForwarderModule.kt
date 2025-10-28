package expo.modules.scrollforwarder

import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition

class ExpoScrollForwarderModule : Module() {
  override fun definition() = ModuleDefinition {
    Name("ExpoScrollForwarder")

    View(ExpoScrollForwarderView::class) {
      Prop("scrollViewTag") { view: ExpoScrollForwarderView, prop: Int ->
        view.scrollViewTag = prop
      }
    }
  }
}