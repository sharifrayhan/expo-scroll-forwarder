const { withPodfile } = require("@expo/config-plugins");

function withExpoScrollForwarder(config) {
  return withPodfile(config, (config) => {
    const podLine =
      "pod 'ExpoScrollForwarder', :path => '../node_modules/expo-scroll-forwarder'";

    if (!config.modResults.contents.includes("ExpoScrollForwarder")) {
      config.modResults.contents = config.modResults.contents.replace(
        /use_expo_modules!\n/,
        `use_expo_modules!\n  ${podLine}\n`,
      );
    }

    return config;
  });
}

module.exports = withExpoScrollForwarder;
