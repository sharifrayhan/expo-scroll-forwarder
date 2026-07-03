import {type ReactNode} from 'react'

export interface ExpoScrollForwarderViewProps {
  /**
   * The native view tag of the scroll view that should receive forwarded
   * scroll gestures. Obtain it with `findNodeHandle(scrollViewRef.current)`
   * once the scroll view is mounted. Pass `null` while the target is not
   * mounted yet; gestures are ignored until a valid tag is provided.
   */
  scrollViewTag: number | null
  /**
   * The content that should capture scroll gestures, typically a header that
   * sits above or on top of the scroll view.
   */
  children?: ReactNode
}
