// Karma configuration
// Generated on Wed Mar 22 2017 08:14:46 GMT+0100 (Västeuropa, normaltid)
'use strict';

module.exports = function(config) {
  config.set({

    // frameworks to use
    // available frameworks: https://npmjs.org/browse/keyword/karma-adapter
    frameworks: ['jasmine'],


    // list of files / patterns to load in the browser
    files: [
      // 'app/*.ts',
      // 'test/*.ts'
      // '../node_modules/es6-shim/es6-shim.js',
      'karma.entry.js'
    ],


    phantomJsLauncher: {
      exitOnResourceError: true
    },


    // preprocess matching files before serving them to the browser
    // available preprocessors: https://npmjs.org/browse/keyword/karma-preprocessor
    preprocessors: {
      'karma.entry.js': ['webpack', 'sourcemap']
      // 'karma.entry.js': ['webpack']
    },


    remapIstanbulReporter: {
      reports: {
        html: 'coverage',
        lcovonly: './coverage/coverage.lcov'
      }
    },

    // test results reporter to use
    // possible values: 'dots', 'progress'
    // available reporters: https://npmjs.org/browse/keyword/karma-reporter
    // reporters: ['dots'],
    reporters: ['progress', 'karma-remap-istanbul'],

    // web server port
    port: 9876,


    // enable / disable colors in the output (reporters and logs)
    colors: true,


    // level of logging
    // possible values: config.LOG_DISABLE || config.LOG_ERROR || config.LOG_WARN || config.LOG_INFO || config.LOG_DEBUG
    logLevel: config.LOG_INFO,


    // enable / disable watching file and executing tests whenever any file changes
    autoWatch: false,


    // start these browsers
    // available browser launchers: https://npmjs.org/browse/keyword/karma-launcher
    browsers: ['PhantomJS'],

    // TO BE ABLE TO DEBUG
    // browsers: ['Chrome'],


    // Continuous Integration mode
    // if true, Karma captures browsers, runs the tests and exits

    // TO BE ABLE TO DEBUG IN CHROME
    // singleRun: false,

    // TO BE ABLE TO RUN IT IN CMD
    singleRun: true,

    // Concurrency level
    // how many browser should be started simultaneous
    concurrency: Infinity,

    webpack: require('../webpack.test.js'),


    webpackMiddleware: {
      // webpack-dev-middleware configuration
      // i.e.
      noInfo: true,
      // and use stats to turn off verbose output
      stats: {
        // options i.e.
        chunks: false
      }
    },

    plugins: [
      require("karma-webpack")
    ]

  });
};
