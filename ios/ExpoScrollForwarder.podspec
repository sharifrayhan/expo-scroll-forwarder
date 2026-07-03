Pod::Spec.new do |s|
  s.name           = 'ExpoScrollForwarder'
  s.version        = '2.0.3'
  s.summary        = 'Forward scroll gestures from a UIView to a UIScrollView'
  s.description    = 'Expo module that forwards pan gestures from a plain view (e.g. a sticky header) to a UIScrollView, with native-feeling deceleration, rubber-banding, and pull-to-refresh.'
  s.author         = 'Rayhan Nafi'
  s.license        = 'MIT'
  s.homepage       = 'https://github.com/sharifrayhan/expo-scroll-forwarder'
  s.platforms      = { :ios => '15.1' }
  s.source         = { git: '' }
  s.static_framework = true

  s.dependency 'ExpoModulesCore'

  # Swift/Objective-C compatibility
  s.pod_target_xcconfig = {
    'DEFINES_MODULE' => 'YES',
    'SWIFT_COMPILATION_MODE' => 'wholemodule'
  }

  s.source_files = "**/*.{h,m,mm,swift,hpp,cpp}"
end
