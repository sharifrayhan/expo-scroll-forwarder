const { withDangerousMod } = require("@expo/config-plugins");
const fs = require("fs");
const path = require("path");

function withExpoScrollForwarder(config) {
  return withDangerousMod(config, [
    "ios",
    (config) => {
      const podfilePath = path.join(
        config.modRequest.projectRoot,
        "ios",
        "Podfile",
      );

      if (fs.existsSync(podfilePath)) {
        const podfile = fs.readFileSync(podfilePath, "utf8");
        if (!podfile.includes("pod 'ExpoScrollForwarder'")) {
          fs.writeFileSync(
            podfilePath,
            podfile +
              `\npod 'ExpoScrollForwarder', :path => '../node_modules/expo-scroll-forwarder'\n`,
          );
        }
      }
      return config;
    },
  ]);
}

module.exports = withExpoScrollForwarder;
