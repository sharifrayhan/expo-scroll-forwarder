Pod::Spec.new do |s|
  s.name         = 'ExpoScrollForwarder'
  s.version      = '1.0.0'
  s.summary      = 'Forward scroll gesture from UIView to UIScrollView'
  s.description  = 'Forward scroll gesture from UIView to UIScrollView'
  s.author       = 'Sharif Rayhan Nafi'
  s.homepage     = 'https://github.com/sharifrayhan/expo-scroll-forwarder'
  s.platforms    = { :ios => '13.4' }
  s.source       = { git: '', tag: s.version } 
  s.static_framework = true
  
  s.dependency 'ExpoModulesCore'
  s.dependency 'React-Core'
  
  s.pod_target_xcconfig = {
    'DEFINES_MODULE' => 'YES',
    'SWIFT_COMPILATION_MODE' => 'wholemodule'
  }
  
  s.source_files = "src/ios/*.{h,m,mm,swift,hpp,cpp}"
end