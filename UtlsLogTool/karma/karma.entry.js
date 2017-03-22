require('es6-shim');
require('reflect-metadata');
require('zone.js/dist/zone');
require('zone.js/dist/long-stack-trace-zone');
require('zone.js/dist/async-test');
require('zone.js/dist/fake-async-test');
require('zone.js/dist/sync-test');
require('zone.js/dist/proxy.js');
require('zone.js/dist/jasmine-patch');

const browserTesting = require('@angular/platform-browser-dynamic/testing');
const coreTesting = require('@angular/core/testing');


// coreTesting.setBaseTestProviders(
//   browserTesting.TEST_BROWSER_DYNAMIC_PLATFORM_PROVIDERS,
//   browserTesting.TEST_BROWSER_DYNAMIC_APPLICATION_PROVIDERS
// );
coreTesting.TestBed.resetTestEnvironment();
coreTesting.TestBed.initTestEnvironment(
  browserTesting.BrowserDynamicTestingModule,
  browserTesting.platformBrowserDynamicTesting()
);

const context = require.context('../test/', true, /\.spec\.ts$/);

context.keys().forEach(context);

Error.stackTraceLimit = Infinity;
jasmine.DEFAULT_TIMEOUT_INTERVAL = 2000;