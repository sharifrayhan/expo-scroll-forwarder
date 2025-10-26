# expo-scroll-forwarder

Forward scroll gestures from a UIView to a UIScrollView in Expo/React Native iOS apps.

## Installation

```bash
npx expo install expo-scroll-forwarder
```

## Usage

```tsx
import { ExpoScrollForwarderView } from "expo-scroll-forwarder";
import { ScrollView } from "react-native";

function App() {
  const scrollViewRef = useRef(null);
  const [scrollViewTag, setScrollViewTag] = useState(null);

  return (
    <View>
      <ExpoScrollForwarderView scrollViewTag={scrollViewTag}>
        {/* Your header content */}
      </ExpoScrollForwarderView>

      <ScrollView
        ref={scrollViewRef}
        onLayout={() => {
          const tag = findNodeHandle(scrollViewRef.current);
          setScrollViewTag(tag);
        }}
      >
        {/* Your scrollable content */}
      </ScrollView>
    </View>
  );
}
```

## Platform Support

- ✅ iOS
- ❌ Android (falls back to children only)

## License

MIT

```

#### **.npmignore**
```

# Development files

_.log
_.swp
.DS_Store
node_modules/
.git/
.gitignore

# Build artifacts

_.xcworkspace
_.xcodeproj
build/
DerivedData/

# IDE

.vscode/
.idea/

```

#### **LICENSE**
```

MIT License

Copyright (c) 2025 [Your Name]

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
