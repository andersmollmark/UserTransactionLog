var path = require('path');
var webpack = require('webpack');
var CommonsChunkPlugin = webpack.optimize.CommonsChunkPlugin;

module.exports = {
  devtool: 'source-map',

  // Bundle different stuff to files that will be imported to index.html
  entry: {
    'deps': [
      'moment/min/moment-with-locales.min.js'
    ],
    'app': './app/app'
  },

  output: {
    path: __dirname + '/build/',
    publicPath: 'build/',
    filename: '[name].js',
    sourceMapFilename: '[name].js.map',
    chunkFilename: '[id].chunk.js'
  },

  resolve: {
    extensions: ['.ts', '.js', '.json', '.css', '.html']
  },

  module: {
    loaders: [
      {
        test: /\.ts$/,
        loaders: [
          'awesome-typescript-loader',
          'angular2-template-loader',
          'angular2-router-loader'
        ]
      },
      {test: /\.css$/, loaders: ['to-string-loader', 'css-loader']},
      {test: /\.html$/, loader: 'raw-loader'}
    ]
  },

  plugins: [
    new CommonsChunkPlugin({name: 'angular2', filename: 'angular2.js', minChunks: Infinity}),
    new CommonsChunkPlugin({name: 'com mon', filename: 'common.js'}),
    // Fixes warning in moment-with-locales.min.js
    //   Module not found: Error: Can't resolve './locale' in ...
    new webpack.IgnorePlugin(/\.\/locale$/),
    new webpack.ContextReplacementPlugin(
      /angular(\\|\/)core(\\|\/)@angular/,
      path.resolve(__dirname, '../app')
    )
  ],
  target: 'electron-renderer'
};