// import {AppConstants} from "./app/app.constants";
const {app, ipcMain, BrowserWindow} = require('electron');

let mainWindow, secondWindow;

function createWindow(){
    mainWindow = new BrowserWindow({
        width: 1280,
        height: 720
    });

    mainWindow.loadURL('file://' + __dirname + '/index.html');
    mainWindow.webContents.openDevTools();

    // Emitted when the window is closed.
    mainWindow.on('closed', () => {
        // Dereference the window object, usually you would store windows
        // in an array if your app supports multi windows, this is the time
        // when you should delete the corresponding element.
        mainWindow = null;
    });

    mainWindow.maximize();
    // secondWindow = new BrowserWindow({
    //     width: 400,
    //     height: 200,
    //     show: false
    // });
    // secondWindow.loadURL('file://' + __dirname + '/app/utlSettingsDialog.html');

    // ipcMain.on(AppConstants.IPC_OPEN_UTL_SETTINGS, () => {
    //     secondWindow.show();
    // });
    //
    // ipcMain.on(AppConstants.IPC_CLOSE_UTL_SETTINGS, () => {
    //     secondWindow.close();
    // });

    // ipcMain.on('openUtlSettings', () => {
    //     secondWindow.show();
    // });
    //
    // ipcMain.on('closeUtlSettings', () => {
    //     secondWindow.close();
    // });
    //
    //
    // // Emitted when the window is closed.
    // secondWindow.on('closed', () => {
    //     // Dereference the window object, usually you would store windows
    //     // in an array if your app supports multi windows, this is the time
    //     // when you should delete the corresponding element.
    //     secondWindow = null;
    // });

}

app.on('ready', createWindow);

// Quit when all windows are closed.
app.on('window-all-closed', () => {
    // On macOS it is common for applications and their menu bar
    // to stay active until the user quits explicitly with Cmd + Q
    if (process.platform !== 'darwin') {
        app.quit()
    }
});

app.on('activate', () => {
    // On macOS it's common to re-create a window in the app when the
    // dock icon is clicked and there are no other windows open.
    if (mainWindow === null) {
        createWindow();
    }
});
