package expo.modules.scrollforwarder

import android.annotation.SuppressLint
import android.content.Context
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.ScrollView
import com.facebook.react.bridge.ReactContext
import com.facebook.react.uimanager.UIManagerHelper
import com.facebook.react.uimanager.events.NativeGestureUtil
import expo.modules.kotlin.AppContext
import expo.modules.kotlin.views.ExpoView
import kotlin.math.abs

/**
 * Forwards vertical drag gestures that start on this view to the React Native
 * scroll view identified by `scrollViewTag`, so a header that sits on top of a
 * list can scroll the list underneath it.
 *
 * Once a touch sequence is recognized as a vertical drag (past the system
 * touch slop and more vertical than horizontal), the motion events are
 * re-dispatched into the scroll view's own onTouchEvent. This means dragging,
 * flinging, and edge effects all use the platform's native scroll physics.
 * Horizontal pans and taps are left alone so children of the header (buttons,
 * horizontal pagers) keep working.
 */
class ExpoScrollForwarderView(context: Context, appContext: AppContext) : ExpoView(context, appContext) {
  var scrollViewTag: Int? = null
    set(value) {
      field = value
      cachedScrollView = null
    }

  private var cachedScrollView: ScrollView? = null
  private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
  private var downX = 0f
  private var downY = 0f
  private var forwarding = false

  override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
    when (ev.actionMasked) {
      MotionEvent.ACTION_DOWN -> {
        downX = ev.x
        downY = ev.y
        forwarding = false
        stopOngoingFling(ev)
      }
      MotionEvent.ACTION_MOVE -> {
        if (shouldStartForwarding(ev)) {
          startForwarding(ev)
          return true
        }
      }
    }
    return super.onInterceptTouchEvent(ev)
  }

  @SuppressLint("ClickableViewAccessibility")
  override fun onTouchEvent(ev: MotionEvent): Boolean {
    when (ev.actionMasked) {
      MotionEvent.ACTION_DOWN -> {
        downX = ev.x
        downY = ev.y
        forwarding = false
      }
      MotionEvent.ACTION_MOVE -> {
        if (!forwarding && shouldStartForwarding(ev)) {
          startForwarding(ev)
        }
        if (forwarding) {
          forwardToScrollView(ev)
        }
      }
      MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
        if (forwarding) {
          forwardToScrollView(ev)
          NativeGestureUtil.notifyNativeGestureEnded(this, ev)
          forwarding = false
        }
      }
    }
    return true
  }

  private fun shouldStartForwarding(ev: MotionEvent): Boolean {
    if (scrollViewTag == null) {
      return false
    }
    val dx = ev.x - downX
    val dy = ev.y - downY
    return abs(dy) > touchSlop && abs(dy) > abs(dx)
  }

  private fun startForwarding(ev: MotionEvent) {
    forwarding = true
    parent?.requestDisallowInterceptTouchEvent(true)

    /*
     * Tell React Native a native gesture took over so JS responders (e.g. an
     * in-flight press on a header button) receive a touch cancel.
     */
    NativeGestureUtil.notifyNativeGestureStarted(this, ev)

    /*
     * Open the scroll view's gesture at the current finger position so its
     * drag state and velocity tracker line up with the rest of the stream.
     */
    dispatchWithAction(ev, MotionEvent.ACTION_DOWN)
  }

  /*
   * Grabbing the header should stop an ongoing fling, just like touching a
   * scroll view does. The DOWN aborts the scroller's animation and the CANCEL
   * immediately ends the synthetic gesture without other side effects.
   */
  private fun stopOngoingFling(ev: MotionEvent) {
    dispatchWithAction(ev, MotionEvent.ACTION_DOWN)
    dispatchWithAction(ev, MotionEvent.ACTION_CANCEL)
  }

  private fun dispatchWithAction(ev: MotionEvent, action: Int) {
    val scrollView = resolveScrollView() ?: return
    val copy = MotionEvent.obtain(ev)
    try {
      copy.action = action
      scrollView.onTouchEvent(copy)
    } finally {
      copy.recycle()
    }
  }

  private fun forwardToScrollView(ev: MotionEvent) {
    val scrollView = resolveScrollView() ?: return
    val copy = MotionEvent.obtain(ev)
    try {
      scrollView.onTouchEvent(copy)
    } finally {
      copy.recycle()
    }
  }

  private fun resolveScrollView(): ScrollView? {
    val tag = scrollViewTag ?: return null

    cachedScrollView?.let {
      if (it.isAttachedToWindow) {
        return it
      }
    }

    val reactContext = appContext.reactContext as? ReactContext ?: return null
    val resolved = try {
      UIManagerHelper.getUIManagerForReactTag(reactContext, tag)?.resolveView(tag)
    } catch (e: Exception) {
      null
    }

    cachedScrollView = findScrollView(resolved, 0)
    return cachedScrollView
  }

  /*
   * The tag may point at the ReactScrollView itself or at a wrapper (e.g. a
   * refresh control), so search a few levels deep for the actual ScrollView.
   * ReactScrollView extends android.widget.ScrollView on both architectures.
   */
  private fun findScrollView(view: View?, depth: Int): ScrollView? {
    if (view == null || depth > 4) {
      return null
    }
    if (view is ScrollView) {
      return view
    }
    if (view is ViewGroup) {
      for (i in 0 until view.childCount) {
        findScrollView(view.getChildAt(i), depth + 1)?.let { return it }
      }
    }
    return null
  }
}
