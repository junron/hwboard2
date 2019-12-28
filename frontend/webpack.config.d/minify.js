if (defined.PRODUCTION) {
  const TerserPlugin = require('terser-webpack-plugin');

  config.optimization = {
    minimize: true,
    minimizer: [new TerserPlugin()]
  }

}
