# expo-scroll-forwarder

[![npm version](https://img.shields.io/npm/v/expo-scroll-forwarder.svg)](https://www.npmjs.com/package/expo-scroll-forwarder)
[![license](https://img.shields.io/npm/l/expo-scroll-forwarder.svg)](https://github.com/sharifrayhan/expo-scroll-forwarder/blob/main/LICENSE)

`expo-scroll-forwarder` is an **iOS-only Expo module** that allows you to forward scroll gestures from a native view to a `ScrollView`. This is useful for creating **custom headers, pull-to-refresh areas, or gesture-forwarding components** in React Native and Expo.

> ⚠️ Android support will be added in a future release.

---

## Features

- Forward vertical scroll gestures from a native view to a `ScrollView`.
- Compatible with Expo modules architecture.
- Pure iOS implementation using Swift and `ExpoModulesCore`.
- Works with `ScrollView` and `RefreshControl`.
- Fully typed with TypeScript for React Native.

---

## Installation

Install the package in your Expo or React Native project:

```bash
npm install expo-scroll-forwarder
# or
yarn add expo-scroll-forwarder
```
