import './polyfills.browser';

import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';

import { AppModule } from './app.module';

export const platformRef = platformBrowserDynamic();

export function main() {

    // process.on('uncaughtException', (err) => {
    //     console.error('whoops! there was an error:' + err);
    //     console.log('now?');
    //     let temp = err;
    //
    // });

    return platformRef.bootstrapModule(AppModule)
        .catch(err => console.error(err));
}

// support async tag or hmr
switch (document.readyState) {
    case 'interactive':
    case 'complete':
        main();
        break;
    case 'loading':
    default:
        document.addEventListener('DOMContentLoaded', () => main());
}
