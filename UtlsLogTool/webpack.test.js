'use strict';

const path = require('path');
const webpack = require('webpack');

module.exports = {

  devtool: 'inline-source-map',

  module: {
    loaders: [
      {
        test: /\.ts$/,
        loader: 'ts',
        exclude: [ /node_modules/ ]
      },
      {
        test: /\.css/,
        loaders: ['style', 'css'],
        include: [ path.resolve(__dirname, "/app"),
          path.resolve(__dirname, "/node_modules/bootstrap")]
      }
    ]
  },

  resolve: {
    extensions: ['', '.js', '.ts'],
    modulesDirectories: ['node_modules'],
    root: path.resolve('.', 'app')
  },

  tslint: {
    emitErrors: true
  }
};