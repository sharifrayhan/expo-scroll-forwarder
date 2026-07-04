# Changelog

## 2.0.5

- iOS: revert the 2.0.4 spinner restart -- it did not fix the static spinner
  and reintroduced a visible secondary content movement. The settle motion is
  back to the single animation from 2.0.3.
- iOS: removed the speculative `sizeToFit()` on the refresh control before
  starting it; manipulating the control's layout right before
  `beginRefreshing` is a plausible cause of the spinner rendering without its
  spin animation.

## 2.0.4 (deprecated -- use 2.0.5)

- iOS: attempted spinner-restart fix; superseded by 2.0.5.

## 2.0.3

- iOS: fix pull-to-refresh in the unpatched (public API) fallback.
  - The spinner is now sized (`sizeToFit`) and started BEFORE the content
    settles, so it is visible and spinning during the return animation
    (previously it could remain invisible).
  - On the new architecture, React Native's RCTPullToRefreshViewComponentView
    reacts to the `refreshing` prop turning true by unconditionally shifting
    contentOffset down by the spinner height, even when the control is
    already refreshing -- which appeared as a second content stretch after
    release. The fallback now detects that one-frame jump and cancels it,
    settling at the spinner's resting offset in a single motion. On the old
    architecture the extra shift never occurs and the detection simply times
    out.

## 2.0.2

- Android: track and forward gestures using raw (screen) coordinates instead
  of view-local ones. The wrapped header typically translates while the drag
  scrolls the list, so local coordinates fed the header's own movement back
  into the gesture -- causing jitter, doubled scroll deltas, and drags that
  stalled and resumed. Raw coordinates only track the finger, which makes
  forwarded scrolling smooth and reliable.

## 2.0.1

- Fix Android Gradle configuration: the library did not set `compileSdk`,
  which failed app builds during configuration ("project ':expo-scroll-forwarder'
  does not specify compileSdk" and a cascading "'release' SoftwareComponent"
  publishing error). The build.gradle now follows the standard expo-modules
  library template: explicit compileSdk/minSdk/targetSdk with rootProject
  overrides, and the react-native dependency resolved through the React
  Native Gradle plugin.

## 2.0.0

Complete rewrite. No code from previous releases (1.x and below) remains.

- iOS: full scroll forwarding implementation with custom deceleration
  physics, rubber-band damping, and pull-to-refresh with haptic feedback.
  Works without any React Native patch (a patched RCTRefreshControl is used
  automatically when present), on both the old (Paper) and new (Fabric)
  architecture.
- Android: native implementation that re-dispatches vertical drags into the
  target scroll view, using the platform's native scroll physics.
- Web: no-op passthrough that renders children.
