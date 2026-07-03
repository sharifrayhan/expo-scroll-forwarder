import ExpoModulesCore
import UIKit

/**
 * Forwards vertical pan gestures from this view to a target UIScrollView, with
 * custom physics that match native scrolling: velocity-based deceleration,
 * rubber-band damping past the top edge, and pull-to-refresh with haptics.
 *
 * Works in any Expo app without patching React Native:
 *
 * - The target is resolved to a plain UIScrollView instead of RCTScrollView,
 *   which works on both the old (Paper) and new (Fabric) architecture and
 *   avoids linking against React headers.
 * - Pull-to-refresh calls RCTRefreshControl's forwarderBeginRefreshing when
 *   the host app has patched one in, and otherwise replicates that behavior
 *   using public UIKit API.
 */
class ExpoScrollForwarderView: ExpoView, UIGestureRecognizerDelegate {
  var scrollViewTag: Int? {
    didSet {
      self.tryFindScrollView()
    }
  }

  private weak var scrollView: UIScrollView?
  private var cancelGestureRecognizers: [UIGestureRecognizer]?
  private var animTimer: Timer?
  private var initialOffset: CGFloat = 0.0
  private var didImpact: Bool = false
  private var refreshShiftObservation: NSKeyValueObservation?

  private var refreshControl: UIRefreshControl? {
    return self.scrollView?.refreshControl
  }

  required init(appContext: AppContext? = nil) {
    super.init(appContext: appContext)

    let pg = UIPanGestureRecognizer(target: self, action: #selector(callOnPan(_:)))
    pg.delegate = self
    self.addGestureRecognizer(pg)

    let tg = UITapGestureRecognizer(target: self, action: #selector(callOnPress(_:)))
    tg.isEnabled = false
    tg.delegate = self

    let lpg = UILongPressGestureRecognizer(target: self, action: #selector(callOnPress(_:)))
    lpg.minimumPressDuration = 0.01
    lpg.isEnabled = false
    lpg.delegate = self

    self.cancelGestureRecognizers = [lpg, tg]
  }

  deinit {
    self.animTimer?.invalidate()
    self.refreshShiftObservation?.invalidate()
  }

  // We don't want to recognize the scroll pan gesture and the swipe back gesture together
  func gestureRecognizer(_ gestureRecognizer: UIGestureRecognizer, shouldRecognizeSimultaneouslyWith otherGestureRecognizer: UIGestureRecognizer) -> Bool {
    if gestureRecognizer is UIPanGestureRecognizer, otherGestureRecognizer is UIPanGestureRecognizer {
      return false
    }

    return true
  }

  // We only want the "scroll" gesture to happen whenever the pan is vertical, otherwise it will
  // interfere with the native swipe back gesture.
  override func gestureRecognizerShouldBegin(_ gestureRecognizer: UIGestureRecognizer) -> Bool {
    guard let gestureRecognizer = gestureRecognizer as? UIPanGestureRecognizer else {
      return true
    }

    let velocity = gestureRecognizer.velocity(in: self)
    return abs(velocity.y) > abs(velocity.x)
  }

  // This will be used to cancel the scroll animation whenever we tap inside of the header. We don't need another
  // recognizer for this one.
  override func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) {
    self.stopTimer()
  }

  // This will be used to cancel the animation whenever we press inside of the scroll view. We don't want to change
  // the scroll view gesture's delegate, so we add an additional recognizer to detect this.
  @IBAction func callOnPress(_ sender: UITapGestureRecognizer) {
    self.stopTimer()
  }

  @IBAction func callOnPan(_ sender: UIPanGestureRecognizer) {
    guard let sv = self.scrollView else {
      return
    }

    let translation = sender.translation(in: self).y

    if sender.state == .began {
      /*
       * A new drag takes ownership of the offset; stop any pending
       * refresh-shift compensation so it cannot fight the finger.
       */
      self.refreshShiftObservation?.invalidate()
      self.refreshShiftObservation = nil

      if sv.contentOffset.y < 0 {
        sv.contentOffset.y = 0
      }

      self.initialOffset = sv.contentOffset.y
    }

    if sender.state == .changed {
      sv.contentOffset.y = self.dampenOffset(-translation + self.initialOffset)

      if sv.contentOffset.y <= -130, !didImpact {
        let generator = UIImpactFeedbackGenerator(style: .light)
        generator.impactOccurred()

        self.didImpact = true
      }
    }

    if sender.state == .ended {
      let velocity = sender.velocity(in: self).y
      self.didImpact = false

      if sv.contentOffset.y <= -130 {
        self.beginRefreshing()
        return
      }

      // A check for a velocity under 250 prevents animations from occurring when they wouldn't in a normal
      // scroll view
      if abs(velocity) < 250, sv.contentOffset.y >= 0 {
        return
      }

      self.startDecayAnimation(translation, velocity)
    }
  }

  func startDecayAnimation(_ translation: CGFloat, _ velocity: CGFloat) {
    guard let sv = self.scrollView else {
      return
    }

    var velocity = velocity

    self.enableCancelGestureRecognizers()

    if velocity > 0 {
      velocity = min(velocity, 5000)
    } else {
      velocity = max(velocity, -5000)
    }

    var animTranslation = -translation
    self.animTimer = Timer.scheduledTimer(withTimeInterval: 1.0 / 120, repeats: true) { [weak self, weak sv] timer in
      guard let self = self, let sv = sv else {
        timer.invalidate()
        return
      }

      velocity *= 0.9875
      animTranslation = (-velocity / 120) + animTranslation

      let nextOffset = self.dampenOffset(animTranslation + self.initialOffset)

      if nextOffset <= 0 {
        if self.initialOffset <= 1 {
          self.scrollToOffset(0)
        } else {
          sv.contentOffset.y = 0
        }

        self.stopTimer()
        return
      } else {
        sv.contentOffset.y = nextOffset
      }

      if abs(velocity) < 5 {
        self.stopTimer()
      }
    }
  }

  func dampenOffset(_ offset: CGFloat) -> CGFloat {
    if offset < 0 {
      return offset - (offset * 0.55)
    }

    return offset
  }

  /*
   * Some apps patch RCTRefreshControl with a forwarderBeginRefreshing method
   * that scrolls the spinner into view and fires the JS onRefresh event. Use
   * it when the host app has such a patch applied; otherwise replicate it
   * with public API.
   *
   * Ordering matters in the fallback: the spinner is sized and started
   * BEFORE the settle animation, so it is visible and spinning while the
   * content returns, and (on the old architecture) RCTRefreshControl's
   * refreshing-state guard prevents a second prop-driven animation. The new
   * architecture needs the additional compensation below.
   */
  func beginRefreshing() {
    guard let sv = self.scrollView, let refreshControl = self.refreshControl else {
      self.scrollToOffset(0)
      return
    }

    let forwarderSelector = NSSelectorFromString("forwarderBeginRefreshing")
    if refreshControl.responds(to: forwarderSelector) {
      refreshControl.perform(forwarderSelector)
      return
    }

    // Ensure the spinner has a real frame even if it has not been laid out yet
    refreshControl.sizeToFit()

    if !refreshControl.isRefreshing {
      refreshControl.beginRefreshing()
      refreshControl.sendActions(for: .valueChanged)
    }

    let restingOffset = -max(refreshControl.frame.height, 60)

    /*
     * On the new architecture, RCTPullToRefreshViewComponentView reacts to
     * the JS `refreshing` prop turning true (which the app's onRefresh
     * handler typically does) by unconditionally shifting contentOffset down
     * by the spinner height -- it checks only the prop transition, not
     * whether the control is already refreshing (RN 0.81,
     * RCTPullToRefreshViewComponentView.mm updateProps /
     * beginRefreshingProgrammatically). That shift lands moments after our
     * settle animation and reads as a second stretch. Watch briefly for that
     * single large downward jump past the resting offset and cancel it. On
     * the old architecture no jump ever comes (RCTRefreshControl guards on
     * its own refreshing state) and the observation just times out.
     */
    self.refreshShiftObservation?.invalidate()
    self.refreshShiftObservation = sv.observe(
      \.contentOffset,
      options: [.old, .new]
    ) { [weak self] scrollView, change in
      guard let self = self,
            let oldOffset = change.oldValue,
            let newOffset = change.newValue else { return }
      let downwardJump = oldOffset.y - newOffset.y
      if downwardJump >= 40, newOffset.y < restingOffset - 20 {
        /*
         * Invalidate before correcting so our own setContentOffset cannot
         * re-enter this observer. Correcting synchronously (from within the
         * offset change notification, like a scroll clamp) prevents the wrong
         * offset from ever rendering.
         */
        self.refreshShiftObservation?.invalidate()
        self.refreshShiftObservation = nil
        scrollView.setContentOffset(CGPoint(x: 0, y: restingOffset), animated: false)
      }
    }
    DispatchQueue.main.asyncAfter(deadline: .now() + 1.5) { [weak self] in
      self?.refreshShiftObservation?.invalidate()
      self?.refreshShiftObservation = nil
    }

    UIView.animate(
      withDuration: 0.3,
      delay: 0,
      options: [.beginFromCurrentState, .curveEaseOut],
      animations: {
        sv.contentOffset = CGPoint(x: 0, y: restingOffset)
      }
    )
  }

  func tryFindScrollView() {
    guard let scrollViewTag = scrollViewTag else {
      return
    }

    // Before we switch to a different scrollview, we always want to remove the cancel gesture recognizer.
    // Otherwise we might end up with duplicates when we switch back to that scrollview.
    self.removeCancelGestureRecognizers()

    let target = self.appContext?.findView(withTag: scrollViewTag, ofType: UIView.self)
    self.scrollView = Self.findScrollView(in: target)

    self.addCancelGestureRecognizers()
  }

  /*
   * The tag may resolve to the scroll view's React wrapper rather than the
   * UIScrollView itself. Paper's RCTScrollView and Fabric's
   * RCTScrollViewComponentView both expose their UIScrollView through a
   * `scrollView` property, which we read dynamically to avoid a compile-time
   * dependency on React headers. As a last resort, search a few levels of
   * subviews.
   */
  private static func findScrollView(in view: UIView?, depth: Int = 0) -> UIScrollView? {
    guard let view = view, depth <= 4 else {
      return nil
    }

    if let scrollView = view as? UIScrollView {
      return scrollView
    }

    if view.responds(to: NSSelectorFromString("scrollView")),
       let scrollView = view.value(forKey: "scrollView") as? UIScrollView {
      return scrollView
    }

    for subview in view.subviews {
      if let scrollView = findScrollView(in: subview, depth: depth + 1) {
        return scrollView
      }
    }

    return nil
  }

  func addCancelGestureRecognizers() {
    self.cancelGestureRecognizers?.forEach { r in
      self.scrollView?.addGestureRecognizer(r)
    }
  }

  func removeCancelGestureRecognizers() {
    self.cancelGestureRecognizers?.forEach { r in
      self.scrollView?.removeGestureRecognizer(r)
    }
  }

  func enableCancelGestureRecognizers() {
    self.cancelGestureRecognizers?.forEach { r in
      r.isEnabled = true
    }
  }

  func disableCancelGestureRecognizers() {
    self.cancelGestureRecognizers?.forEach { r in
      r.isEnabled = false
    }
  }

  func scrollToOffset(_ offset: CGFloat, animated: Bool = true) {
    self.scrollView?.setContentOffset(CGPoint(x: 0, y: offset), animated: animated)
  }

  func stopTimer() {
    self.disableCancelGestureRecognizers()
    self.animTimer?.invalidate()
    self.animTimer = nil
  }
}
