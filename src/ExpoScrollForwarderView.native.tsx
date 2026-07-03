import {type ComponentType} from 'react'
import {requireNativeViewManager} from 'expo-modules-core'

import {type ExpoScrollForwarderViewProps} from './ExpoScrollForwarder.types'

const NativeView: ComponentType<ExpoScrollForwarderViewProps> =
  requireNativeViewManager('ExpoScrollForwarder')

/**
 * Wraps content (typically a header) and forwards vertical pan gestures that
 * start on it to the scroll view identified by `scrollViewTag`.
 */
export function ExpoScrollForwarderView({
  children,
  ...rest
}: ExpoScrollForwarderViewProps) {
  return <NativeView {...rest}>{children}</NativeView>
}
