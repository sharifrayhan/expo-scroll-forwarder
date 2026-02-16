Pod::Spec.new do |s|
  s.name         = 'ExpoScrollForwarder'
  s.version      = '1.0.3'
  s.summary      = 'Forward scroll gesture from UIView to UIScrollView'
  s.description  = 'Forward scroll gesture from UIView to UIScrollView'
  s.author       = 'Sharif Rayhan Nafi'
  s.homepage     = 'https://github.com/sharifrayhan/expo-scroll-forwarder'
  s.license      = 'MIT'
  s.platforms    = { :ios => '13.4' }

  # Use local path for development/EAS builds
  s.source       = { :path => '.' }

  s.static_framework = true
  s.dependency 'ExpoModulesCore'
  s.dependency 'React-Core'

  s.pod_target_xcconfig = {
    'DEFINES_MODULE' => 'YES',
    'SWIFT_COMPILATION_MODE' => 'wholemodule'
  }

  # Include all Swift files
  s.source_files = "src/ios/*.{swift,h,m,mm,hpp,cpp}"
end
