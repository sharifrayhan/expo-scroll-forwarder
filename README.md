# expo-scroll-forwarder

A native module for React Native and Expo that forwards scroll gestures from a non-scrollable view to a ScrollView. Perfect for creating sticky headers that can initiate scrolling, or any UI where you want touch gestures on one view to control scrolling in another.

## Features

- ✨ Forward scroll gestures from any view to a ScrollView
- 📱 Works on both iOS and Android
- 🎯 Supports pull-to-refresh
- 🚀 Smooth momentum scrolling with decay animation
- 💫 Haptic feedback at refresh threshold
- 🎨 Natural damping for overscroll
- 👆 Tap to cancel scroll momentum

## Installation

```bash
npx expo install expo-scroll-forwarder
```

## Usage

```typescript
import React, { useRef, useState, useEffect } from "react";
import { ExpoScrollForwarderView } from "expo-scroll-forwarder";
import {
  ScrollView,
  View,
  Text,
  RefreshControl,
  findNodeHandle,
} from "react-native";

export default function App() {
  const scrollViewRef = useRef<ScrollView>(null);
  const [scrollViewTag, setScrollViewTag] = useState<number | null>(null);
  const [refreshing, setRefreshing] = useState(false);

  // Get the native tag of the ScrollView
  useEffect(() => {
    if (scrollViewRef.current) {
      const tag = findNodeHandle(scrollViewRef.current);
      setScrollViewTag(tag);
    }
  }, []);

  const onRefresh = () => {
    setRefreshing(true);
    setTimeout(() => setRefreshing(false), 2000);
  };

  return (
    <>
      {/* Header that forwards scroll gestures */}
      <ExpoScrollForwarderView scrollViewTag={scrollViewTag}>
        <View style={{ padding: 20, backgroundColor: "#6366f1" }}>
          <Text style={{ color: "white", fontSize: 20 }}>
            Swipe down here to scroll
          </Text>
        </View>
      </ExpoScrollForwarderView>

      {/* Main ScrollView */}
      <ScrollView
        ref={scrollViewRef}
        refreshControl={
          <RefreshControl refreshing={refreshing} onRefresh={onRefresh} />
        }
      >
        <View style={{ padding: 20 }}>
          <Text>Your scrollable content here</Text>
          {/* Add more content... */}
        </View>
      </ScrollView>
    </>
  );
}
```

## API Reference

### ExpoScrollForwarderView

A view component that forwards scroll gestures to a target ScrollView.

#### Props

| Prop            | Type              | Required | Description                                                                                                        |
| --------------- | ----------------- | -------- | ------------------------------------------------------------------------------------------------------------------ |
| `scrollViewTag` | `number \| null`  | Yes      | The native tag of the ScrollView to forward gestures to. Obtain this using `findNodeHandle(scrollViewRef.current)` |
| `children`      | `React.ReactNode` | Yes      | The content that will capture and forward scroll gestures                                                          |

## How It Works

1. **Get ScrollView Tag**: Use `findNodeHandle()` to get the native tag of your ScrollView
2. **Wrap Your Header**: Wrap any view in `ExpoScrollForwarderView` and pass the tag
3. **Gesture Forwarding**: Touch gestures on the wrapped view are forwarded to the ScrollView
4. **Natural Scrolling**: Includes momentum, damping, and all native scroll behaviors

## Use Cases

- **Collapsible Headers**: Create headers that users can swipe to scroll the content below
- **Custom Navigation Bars**: Make navigation bars scrollable
- **Dashboard Cards**: Allow cards or panels to initiate scrolling
- **Video Player Controls**: Overlay controls that don't block scroll gestures
- **Chat Input Areas**: Input fields that can still control chat scroll

## Platform Support

- ✅ iOS 13.4+
- ✅ Android API 21+
- ✅ Expo SDK 50+

## Example

Check out the [example app](./example) in the repository for a full working demo.

## Technical Details

### iOS Implementation

- Uses `UIPanGestureRecognizer` for gesture detection
- Custom decay animation with configurable friction
- Integrates with native `UIScrollView` and `RCTRefreshControl`
- Prevents conflicts with swipe-back gestures

### Android Implementation

- Uses `GestureDetector` and custom touch event handling
- `ValueAnimator` for smooth decay animations
- Integrates with `ReactScrollView` and RefreshControl
- Proper velocity tracking and momentum calculation

## Troubleshooting

### ScrollView not responding to gestures

Make sure you're correctly getting and setting the `scrollViewTag`:

```typescript
const tag = findNodeHandle(scrollViewRef.current);
setScrollViewTag(tag);
```

### Gestures interfering with other touch handlers

The module automatically handles gesture conflicts, but ensure your view hierarchy is correct - the `ExpoScrollForwarderView` should be a sibling or parent of the ScrollView, not a child.

### Pull-to-refresh not working

Ensure you've added a `RefreshControl` to your ScrollView:

```typescript
<ScrollView
  refreshControl={
    <RefreshControl refreshing={refreshing} onRefresh={onRefresh} />
  }
>
```

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

MIT © [Sharif Rayhan Nafi](https://github.com/sharifrayhan)

## Author

**Sharif Rayhan Nafi**

- GitHub: [@sharifrayhan](https://github.com/sharifrayhan)
- Email: sharifrayhan.official@gmail.com

## Acknowledgments

This module was inspired by the scroll forwarding behavior in the Bluesky app
.
Special thanks to the Bluesky engineering team — their implementation and user experience provided valuable insight into creating a natural, responsive gesture-forwarding system.

Built with [Expo Modules](https://docs.expo.dev/modules/) 🚀
