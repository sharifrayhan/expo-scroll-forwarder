# Changelog

## 2.0.0

Complete rewrite. No code from previous releases (1.x and below) remains.

- iOS: full scroll forwarding implementation ported from Bluesky's in-tree
  `expo-scroll-forwarder` module (custom deceleration physics, rubber-band
  damping, pull-to-refresh with haptic feedback). Works without Bluesky's
  React Native patch (and automatically uses the patch when it is applied),
  and works on both the old (Paper) and new (Fabric) architecture.
- Android: new native implementation that re-dispatches vertical drags into
  the target scroll view, using the platform's native scroll physics.
- Web: no-op passthrough that renders children.
