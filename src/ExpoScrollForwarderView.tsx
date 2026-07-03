import {type ExpoScrollForwarderViewProps} from './ExpoScrollForwarder.types'

/**
 * Web fallback. Scroll forwarding is a native-only interaction, so on web this
 * simply renders its children.
 */
export function ExpoScrollForwarderView({
  children,
}: ExpoScrollForwarderViewProps) {
  return <>{children}</>
}
