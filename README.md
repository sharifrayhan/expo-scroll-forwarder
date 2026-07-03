# expo-scroll-forwarder

An Expo native module that forwards scroll gestures from a plain view to a
scroll view. This lets a fixed or sticky header (that is not itself inside the
scroll view) respond to drag gestures and scroll the content below it -- the
interaction seen on profile screens in popular social apps, where dragging the
profile header scrolls the feed underneath.

> **Note:** version 2.x is a complete rewrite. It shares no code with, and is
> not compatible with, releases 1.x and below.

## Features

- **iOS**: pan gestures on the wrapper are translated into scroll offsets with
  custom physics that feel native:
  - velocity-based deceleration (fling) animation at 120 fps
  - rubber-band damping when dragging past the top edge
  - pull-to-refresh at -130 pt with light haptic feedback
  - only activates on vertical pans, so it never fights the swipe-back gesture
  - tapping anywhere cancels an in-flight deceleration, like a real scroll view
- **Android**: vertical drags that start on the wrapper are re-dispatched into
  the target scroll view's own touch handler, so dragging and flinging use the
  platform's native scroll physics. Taps and horizontal pans pass through to
  the header's children untouched.
- **Web**: no-op; the wrapper just renders its children.

## Requirements

- Expo SDK 52+ (built and tested against SDK 54)
- A development build or bare workflow -- this is a native module, so it does
  **not** work in Expo Go
- iOS 15.1+

Works on both the old (Paper) and new (Fabric) React Native architecture: the
native code resolves the target as a plain platform scroll view rather than an
architecture-specific wrapper.

## Installation

```sh
npx expo install expo-scroll-forwarder
```

Then rebuild your native app:

```sh
npx expo prebuild
npx expo run:ios     # or: npx expo run:android / eas build
```

No config plugin is needed; Expo autolinking picks the module up
automatically.

## Usage

Wrap the view that should capture scroll gestures (typically your header) in
`ExpoScrollForwarderView` and give it the native tag of the scroll view it
should drive:

```tsx
import {useRef, useState} from 'react'
import {findNodeHandle, FlatList, View} from 'react-native'
import {ExpoScrollForwarderView} from 'expo-scroll-forwarder'

export function ProfileScreen() {
  const listRef = useRef<FlatList>(null)
  const [scrollViewTag, setScrollViewTag] = useState<number | null>(null)

  return (
    <View style={{flex: 1}}>
      <ExpoScrollForwarderView scrollViewTag={scrollViewTag}>
        <ProfileHeader />
      </ExpoScrollForwarderView>

      <FlatList
        ref={node => {
          listRef.current = node
          setScrollViewTag(findNodeHandle(node))
        }}
        data={posts}
        renderItem={renderPost}
      />
    </View>
  )
}
```

With multiple tabs/lists under one header (a profile screen with Posts /
Replies / Media tabs, for example), update the tag whenever the focused list
changes:

```tsx
useEffect(() => {
  if (isFocused && listRef.current) {
    setScrollViewTag(findNodeHandle(listRef.current))
  }
}, [isFocused])
```

### Props

| Prop            | Type             | Description                                                                                           |
| --------------- | ---------------- | ----------------------------------------------------------------------------------------------------- |
| `scrollViewTag` | `number \| null` | Native view tag of the target scroll view, from `findNodeHandle`. Gestures are ignored while `null`.   |
| `children`      | `ReactNode`      | The content that captures scroll gestures.                                                             |

The target can be a `ScrollView`, `FlatList`, or anything else backed by a
native vertical scroll view.

## Pull-to-refresh notes (iOS)

When the user drags past -130 pt, the module triggers the target's
`RefreshControl`:

- If the host app has a patched `RCTRefreshControl` exposing a
  `forwarderBeginRefreshing` method, the module detects it at runtime and
  calls it.
- Otherwise the module handles it with public UIKit API: it starts the
  spinner and emits the `valueChanged` control event (which fires the JS
  `onRefresh` handler), then settles the content at the spinner's resting
  offset in a single 0.3 s animation. On the new architecture it also
  compensates for React Native's prop-driven begin-refresh, which would
  otherwise shift the content a second time.

So `onRefresh` works out of the box, with or without a patch, on both
architectures.

## Android notes

The Android implementation re-dispatches the touch stream into the target
scroll view, which keeps native fling physics. Differences from iOS:

- Pull-to-refresh forwarding is not implemented (Android's `RefreshControl`
  is a `SwipeRefreshLayout` wrapper that only reacts to touches on itself).
- There is no haptic feedback, since the platform scroll physics handle the
  gesture end-to-end.

### Disabling forwarding on Android

The native view only activates when it has a tag, so passing `null` turns it
into a plain container:

```tsx
<ExpoScrollForwarderView
  scrollViewTag={Platform.OS === 'ios' ? scrollViewTag : null}>
  <ProfileHeader />
</ExpoScrollForwarderView>
```

### Alternative: layout pass-through on Android

Instead of forwarding gestures, you can render the header as an
absolutely-positioned overlay on top of the list and, on Android only, mark
it `pointerEvents="box-none"`. Touches on non-interactive parts of the header
then pass through to the list behind it, which scrolls natively -- no native
module involved. Buttons and other touchables inside the header keep working
because `box-none` only makes the container itself transparent to touches,
not its children.

A minimal version of that pattern (with a collapsing header):

```tsx
import {useState} from 'react'
import {Platform, View} from 'react-native'
import Animated, {
  useAnimatedScrollHandler,
  useAnimatedStyle,
  useSharedValue,
} from 'react-native-reanimated'

export function ProfileScreen() {
  const [headerHeight, setHeaderHeight] = useState(0)
  const scrollY = useSharedValue(0)

  const onScroll = useAnimatedScrollHandler(e => {
    scrollY.value = e.contentOffset.y
  })

  /* Slide the header out of view as the list scrolls (optional) */
  const headerStyle = useAnimatedStyle(() => ({
    transform: [
      {translateY: -Math.min(Math.max(scrollY.value, 0), headerHeight)},
    ],
  }))

  return (
    <View style={{flex: 1}}>
      {/*
        The list fills the whole screen. Its content is pushed down by
        headerHeight so nothing hides behind the header, but the native
        scroll surface physically extends underneath it.
      */}
      <Animated.FlatList
        data={posts}
        renderItem={renderPost}
        onScroll={onScroll}
        contentContainerStyle={{paddingTop: headerHeight}}
        scrollIndicatorInsets={{top: headerHeight}}
        progressViewOffset={headerHeight}
      />

      {/*
        The header overlays the top of the list. On Android, box-none lets
        drags on non-interactive areas fall through to the list underneath,
        which scrolls natively. On iOS, touches never fall through overlaid
        views this way, which is why the forwarder exists.
      */}
      <Animated.View
        pointerEvents={Platform.OS === 'ios' ? 'auto' : 'box-none'}
        onLayout={e => setHeaderHeight(e.nativeEvent.layout.height)}
        style={[
          {position: 'absolute', top: 0, left: 0, right: 0, zIndex: 1},
          headerStyle,
        ]}>
        <ExpoScrollForwarderView
          scrollViewTag={Platform.OS === 'ios' ? scrollViewTag : null}>
          <ProfileHeader />
        </ExpoScrollForwarderView>
      </Animated.View>
    </View>
  )
}
```

Requirements for the pass-through to work on Android:

- The list must be rendered *under* the header (header after the list in
  JSX, or higher `zIndex`), occupying the same screen area.
- Every wrapper between the screen and the header content must be
  `box-none` (or have no background/touch handlers), otherwise it swallows
  the touch before it reaches the list.
- Drags that start on a touchable (a button, a pressable image) will not
  scroll -- the touchable claims the gesture.

## How it works

- **iOS** (`ios/ExpoScrollForwarderView.swift`): attaches a
  `UIPanGestureRecognizer` to the wrapper, resolves the target `UIScrollView`
  by React tag through `AppContext.findView`, and drives `contentOffset`
  directly. On release it runs a decay animation (velocity clamped to +/-5000
  pt/s, decaying by 0.9875 per frame at 120 fps) and stops it if the user
  touches the header or the scroll view.
- **Android** (`android/.../ExpoScrollForwarderView.kt`): overrides
  `onInterceptTouchEvent` to detect a vertical drag past the touch slop,
  notifies React Native that a native gesture started (so JS presses get
  cancelled), then forwards copies of the motion events into the scroll view's
  `onTouchEvent`.

## Development / publishing

```sh
cd expo-scroll-forwarder
npm install          # installs dev dependencies and builds
npm run build        # compiles src/ to build/ with type declarations
npm publish          # prepublishOnly runs the build automatically
```

## Disclaimer

This is a community package and is not affiliated with or endorsed by Expo.

## License

MIT
