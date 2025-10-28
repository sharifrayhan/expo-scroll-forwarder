package expo.modules.scrollforwarder

import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition

class ExpoScrollForwarderModule : Module() {
  override fun definition() = ModuleDefinition {
    Name("ExpoScrollForwarder")

    // Use ViewManager instead of View
    ViewManager {
      View { context, appContext ->
        ExpoScrollForwarderView(context, appContext)
      }

      Prop("scrollViewTag") { view: ExpoScrollForwarderView, prop: Int ->
        view.scrollViewTag = prop
      }
    }
  }
}