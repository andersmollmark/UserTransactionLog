Tool to open and read json-files from Utl-servern that contains logs.

Type 'npm install' to install all necessary files to be able to build and run the project.
The folder 'node_modules' should have been created.

When npm is done, type 'npm run build' to create a build of the project.
Now the folder 'build' should have been created and now its ready to launch the UtlsLogTool.

Type 'npm run start' and it will start an instance of the tool. To test, click on menu and open and choose the dumpmini.json that resides inside project.

There is a possibility package this tool as an executable file, to windows (32- and 64-bit), linux and mac (osx).
To do that, type 'npm run package-windows' for windows, 'npm run package-linux' for linux and finally 'npm run package-osx' for mac.
The result will end up under a folder called dist in project-root.

Unfortunately, when running build, and there is a package with executable files, it fails.
The sollution to this is just to either remove the packages or just move it to another destination. Then its possible to run build again.