
const {app, ipcMain, BrowserWindow} = require('electron');

// require('electron-debug')({showDevTools: true});

let mainWindow, devtoolOpen;

function createWindow(){
    mainWindow = new BrowserWindow({
        width: 1280,
        height: 720
    });

    devtoolOpen = false;

    mainWindow.loadURL('file://' + __dirname + '/index.html');

    // mainWindow.webContents.openDevTools();

    ipcMain.on('TOGGLE_DEV_TOOLS', (event, arg) => {
       if(devtoolOpen){
           mainWindow.webContents.closeDevTools();
       }
       else{
           mainWindow.webContents.openDevTools();
       }
        devtoolOpen = !devtoolOpen;
    });

    // Emitted when the window is closed.
    mainWindow.on('closed', () => {
        // Dereference the window object, usually you would store windows
        // in an array if your app supports multi windows, this is the time
        // when you should delete the corresponding element.
        mainWindow = null;
    });

    mainWindow.maximize();
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
