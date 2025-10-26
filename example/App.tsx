import React, { useRef, useState } from "react";
import { ExpoScrollForwarderView } from "expo-scroll-forwarder";
import {
  SafeAreaView,
  ScrollView,
  Text,
  View,
  StyleSheet,
  RefreshControl,
  findNodeHandle,
} from "react-native";

export default function App() {
  const scrollViewRef = useRef<ScrollView>(null);
  const [scrollViewTag, setScrollViewTag] = useState<number | null>(null);
  const [refreshing, setRefreshing] = useState(false);

  const onRefresh = React.useCallback(() => {
    setRefreshing(true);
    setTimeout(() => {
      setRefreshing(false);
    }, 2000);
  }, []);

  // Get the native tag of the ScrollView
  React.useEffect(() => {
    if (scrollViewRef.current) {
      const tag = findNodeHandle(scrollViewRef.current);
      setScrollViewTag(tag);
    }
  }, []);

  return (
    <SafeAreaView style={styles.container}>
      {/* Header that forwards scroll gestures */}
      <ExpoScrollForwarderView scrollViewTag={scrollViewTag}>
        <View style={styles.header}>
          <Text style={styles.headerTitle}>Scroll Forwarder Demo</Text>
          <Text style={styles.headerSubtitle}>
            Swipe down anywhere on this header to scroll
          </Text>
        </View>
      </ExpoScrollForwarderView>

      {/* Main ScrollView */}
      <ScrollView
        ref={scrollViewRef}
        style={styles.scrollView}
        refreshControl={
          <RefreshControl refreshing={refreshing} onRefresh={onRefresh} />
        }
      >
        <View style={styles.content}>
          <Text style={styles.sectionTitle}>How it works:</Text>
          <Text style={styles.description}>
            The header above is a non-scrollable view, but gestures on it are
            forwarded to this ScrollView.
          </Text>

          <Text style={styles.sectionTitle}>Try these gestures:</Text>
          <View style={styles.instructionBox}>
            <Text style={styles.instruction}>
              • Swipe down on the header to scroll down
            </Text>
            <Text style={styles.instruction}>
              • Swipe up on the header to scroll up
            </Text>
            <Text style={styles.instruction}>
              • Pull down far enough to trigger refresh
            </Text>
            <Text style={styles.instruction}>
              • Tap the header to cancel scroll momentum
            </Text>
          </View>

          {/* Generate some content to scroll */}
          {Array.from({ length: 30 }, (_, i) => (
            <View key={i} style={styles.item}>
              <Text style={styles.itemText}>Item {i + 1}</Text>
            </View>
          ))}
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#f5f5f5",
  },
  header: {
    backgroundColor: "#6366f1",
    padding: 20,
    paddingTop: 10,
    paddingBottom: 30,
  },
  headerTitle: {
    fontSize: 24,
    fontWeight: "bold",
    color: "white",
    marginBottom: 8,
  },
  headerSubtitle: {
    fontSize: 14,
    color: "rgba(255, 255, 255, 0.9)",
  },
  scrollView: {
    flex: 1,
  },
  content: {
    padding: 20,
  },
  sectionTitle: {
    fontSize: 20,
    fontWeight: "600",
    marginTop: 20,
    marginBottom: 12,
    color: "#1f2937",
  },
  description: {
    fontSize: 16,
    lineHeight: 24,
    color: "#4b5563",
    marginBottom: 10,
  },
  instructionBox: {
    backgroundColor: "white",
    padding: 16,
    borderRadius: 12,
    marginBottom: 20,
    shadowColor: "#000",
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  instruction: {
    fontSize: 15,
    lineHeight: 24,
    color: "#374151",
    marginBottom: 8,
  },
  item: {
    backgroundColor: "white",
    padding: 20,
    marginBottom: 10,
    borderRadius: 8,
    shadowColor: "#000",
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.05,
    shadowRadius: 2,
    elevation: 2,
  },
  itemText: {
    fontSize: 16,
    color: "#1f2937",
  },
});
