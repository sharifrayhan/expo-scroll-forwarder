package expo.modules.scrollforwarder

import android.animation.ValueAnimator
import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.animation.DecelerateInterpolator
import androidx.core.view.GestureDetectorCompat
import com.facebook.react.views.scroll.ReactScrollView
import expo.modules.kotlin.AppContext
import expo.modules.kotlin.views.ExpoView
import kotlin.math.abs

class ExpoScrollForwarderView(context: Context, appContext: AppContext) : ExpoView(context, appContext) {
  
  var scrollViewTag: Int? = null
    set(value) {
      field = value
      tryFindScrollView()
    }

  private var reactScrollView: ReactScrollView? = null
  private var gestureDetector: GestureDetectorCompat
  private var isScrolling = false
  private var initialScrollY = 0
  private var lastY = 0f
  private var decayAnimator: ValueAnimator? = null
  private var didImpact = false

  init {
    gestureDetector = GestureDetectorCompat(context, GestureListener())
  }

  override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
    val scrollView = reactScrollView ?: return super.onInterceptTouchEvent(ev)
    
    when (ev.action) {
      MotionEvent.ACTION_DOWN -> {
        stopDecayAnimation()
        lastY = ev.rawY
        initialScrollY = scrollView.scrollY
        isScrolling = false
      }
      MotionEvent.ACTION_MOVE -> {
        val deltaY = lastY - ev.rawY
        
        // Check if this is a vertical scroll gesture
        if (!isScrolling && abs(deltaY) > 10) {
          // Calculate if the gesture is more vertical than horizontal
          val initialX = if (ev.historySize > 0) ev.getHistoricalX(0) else ev.x
          val initialY = if (ev.historySize > 0) ev.getHistoricalY(0) else ev.y
          val deltaX = ev.x - initialX
          val deltaYCheck = ev.y - initialY
          
          if (abs(deltaYCheck) > abs(deltaX)) {
            isScrolling = true
            return true
          }
        }
      }
    }
    
    return isScrolling || super.onInterceptTouchEvent(ev)
  }

  override fun onTouchEvent(event: MotionEvent): Boolean {
    val scrollView = reactScrollView ?: return super.onTouchEvent(event)
    
    gestureDetector.onTouchEvent(event)
    
    when (event.action) {
      MotionEvent.ACTION_DOWN -> {
        stopDecayAnimation()
        lastY = event.rawY
        initialScrollY = scrollView.scrollY
        if (scrollView.scrollY < 0) {
          scrollView.scrollY = 0
        }
        return true
      }
      
      MotionEvent.ACTION_MOVE -> {
        val deltaY = (lastY - event.rawY).toInt()
        val newOffset = dampenOffset(initialScrollY + deltaY)
        
        scrollView.scrollY = newOffset
        
        // Haptic feedback at refresh threshold
        if (newOffset <= -130 && !didImpact) {
          performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
          didImpact = true
        }
        
        return true
      }
      
      MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
        val velocityY = calculateVelocity(event)
        didImpact = false
        
        // Check for pull-to-refresh threshold
        if (scrollView.scrollY <= -130) {
          triggerRefresh(scrollView)
          return true
        }
        
        // Don't animate if velocity is too low and we're at a valid position
        if (abs(velocityY) < 250 && scrollView.scrollY >= 0) {
          isScrolling = false
          return true
        }
        
        startDecayAnimation(scrollView, velocityY.toFloat())
        isScrolling = false
        return true
      }
    }
    
    return super.onTouchEvent(event)
  }

  private fun calculateVelocity(event: MotionEvent): Int {
    if (event.historySize == 0) return 0
    
    val lastHistoricalY = event.getHistoricalY(event.historySize - 1)
    val lastHistoricalTime = event.getHistoricalEventTime(event.historySize - 1)
    val deltaY = event.y - lastHistoricalY
    val deltaTime = (event.eventTime - lastHistoricalTime) / 1000f
    
    return if (deltaTime > 0) {
      (-deltaY / deltaTime).toInt()
    } else {
      0
    }
  }

  private fun startDecayAnimation(scrollView: ReactScrollView, velocity: Float) {
    var currentVelocity = velocity.coerceIn(-5000f, 5000f)
    val startOffset = scrollView.scrollY
    var currentOffset = startOffset.toFloat()
    
    decayAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
      duration = 3000
      interpolator = DecelerateInterpolator()
      
      addUpdateListener { animator ->
        currentVelocity *= 0.9875f
        currentOffset += (-currentVelocity / 120f)
        
        val newOffset = dampenOffset(currentOffset.toInt())
        
        when {
          newOffset <= 0 -> {
            if (startOffset <= 1) {
              scrollView.smoothScrollTo(0, 0)
            } else {
              scrollView.scrollY = 0
            }
            cancel()
          }
          else -> {
            scrollView.scrollY = newOffset
          }
        }
        
        if (abs(currentVelocity) < 5) {
          cancel()
        }
      }
    }
    decayAnimator?.start()
  }

  private fun dampenOffset(offset: Int): Int {
    return if (offset < 0) {
      (offset - (offset * 0.55)).toInt()
    } else {
      offset
    }
  }

  private fun triggerRefresh(scrollView: ReactScrollView) {
    // Trigger refresh by finding and activating the refresh control
    try {
      for (i in 0 until scrollView.childCount) {
        val child = scrollView.getChildAt(i)
        if (child.javaClass.simpleName.contains("Refresh")) {
          // Try to invoke setRefreshing via reflection
          val method = child.javaClass.getMethod("setRefreshing", Boolean::class.javaPrimitiveType)
          method.invoke(child, true)
          break
        }
      }
    } catch (e: Exception) {
      // Fallback: just scroll to trigger position
      scrollView.scrollY = -140
    }
  }

  private fun stopDecayAnimation() {
    decayAnimator?.cancel()
    decayAnimator = null
  }

  private fun tryFindScrollView() {
    val tag = scrollViewTag ?: return
    
    reactScrollView = appContext?.findView(tag) as? ReactScrollView
  }

  inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
    override fun onDown(e: MotionEvent): Boolean {
      stopDecayAnimation()
      return true
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
      stopDecayAnimation()
      return true
    }
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    stopDecayAnimation()
  }
}