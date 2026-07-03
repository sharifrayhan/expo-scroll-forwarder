# Changelog

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
