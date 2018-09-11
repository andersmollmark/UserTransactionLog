import {Component, NgZone, OnInit} from "@angular/core";
import {UtlsFileService} from "./utls-file.service";
import {UtlsLog} from "./log";
import {Observable} from "rxjs/Observable";
import {Dto} from "./dto";
import {AppConstants} from "./app.constants";
import * as _ from "lodash";
import {TimeFilterService} from "./timefilter.service";
import {SortingObject} from "./sortingObject";
import {UtlserverService} from "./utlserver.service";
import {View} from "./view";
import {FetchLogParam} from "./fetchLogParam";

const electron = require('electron');
const remote = electron.remote;
const ipcRenderer = electron.ipcRenderer;

let {dialog} = remote;

@Component({
    selector: 'my-app',
    templateUrl: './app.component.html'

})
export class AppComponent implements OnInit {

    logs$: Observable<UtlsLog[]>;
    public filterQuery;
    columnSortValue: string = "";
    columnSortObject: SortingObject = new SortingObject();
    public isLoaded: boolean;
    public sortBy = "";
    logfileName: string = '';

    // Select-column-filter
    selectedColumnDefaultChoice = "--- Select column ---";
    public selectedColumn = this.selectedColumnDefaultChoice;
    lastSelectedColumn = "";
    allColumns = AppConstants.STR_ALL;
    cols = [
        {name: "Username", value: AppConstants.COL_USERNAME},
        {name: "Active tab", value: AppConstants.COL_TAB},
        {name: "Category", value: AppConstants.COL_CATEGORY},
        {name: "Eventname", value: AppConstants.COL_EVENTNAME}
    ];

    // Selected-column-filter
    selectedContentDefaultChoice = "--- Select ---";
    public selectedContent = this.selectedContentDefaultChoice;
    allContent = AppConstants.STR_ALL;
    constants = AppConstants;
    columnContent: Dto[];

    oldViewname: string;
    activeViewname: string = AppConstants.VIEW_EMPTY;
    views: View[] = new Array<View>();

    menu;

    constructor(private utlsFileService: UtlsFileService, private timeFilterService: TimeFilterService,
                private utlserverService: UtlserverService, private zone: NgZone) {
        this.isLoaded = false;
        this.initViews();
    }

    private initViews() {
        this.views[AppConstants.VIEW_LOGS] = new View(AppConstants.VIEW_LOGS, false);
        this.views[AppConstants.VIEW_SETTINGS] = new View(AppConstants.VIEW_SETTINGS, false);
        this.views[AppConstants.VIEW_EMPTY] = new View(AppConstants.VIEW_EMPTY, false);
        this.views[AppConstants.VIEW_WAIT] = new View(AppConstants.VIEW_WAIT, false);
        this.views[AppConstants.VIEW_FETCH_LOGS] = new View(AppConstants.VIEW_FETCH_LOGS, false);
    }

    ngOnInit(): void {
        let self = this;
        this.menu = remote.Menu.buildFromTemplate([{
            label: 'Menu',
            submenu: [
                {
                    label: 'Open logfile',
                    click: function () {
                        self.zone.run(() => {
                            dialog.showOpenDialog(self.checkFilenameAndHandleFile);
                        });
                    }
                },
                {
                    label: self.getIpMenuLabel(),
                    click: function () {
                        self.zone.run(() => self.showView(AppConstants.VIEW_SETTINGS));
                    }
                },
                {
                    label: 'Fetch logfile',
                    click: function () {
                        self.utlsFileService.setOpenWhenFileIsFetched(false);
                        self.showView(AppConstants.VIEW_FETCH_LOGS);
                    }
                },
                {
                    label: 'Fetch and open logfile',
                    click: function () {
                        self.utlsFileService.setOpenWhenFileIsFetched(true);
                        self.showView(AppConstants.VIEW_FETCH_LOGS);
                    }
                },
                {
                    label: 'Toggle devtool',
                    accelerator: 'Ctrl+Shift+I',
                    click: function () {
                        ipcRenderer.send('TOGGLE_DEV_TOOLS');
                    }
                }
            ]
        }]);
        remote.Menu.setApplicationMenu(this.menu);

    }

    ngDoCheck() {
        this.filterQuery = this.timeFilterService.getFilterQuery();
        if (this.utlsFileService.isColumnContentChanged()) {
            this.changeColumnValueAndContentValues(this.selectedColumn);
        }
    }

    private getIpMenuLabel(): string {
        return 'Change ip for utlserver (now: '.concat(this.utlserverService.getUtlsIp()).concat(')');
    }

    closeSettings(): void {
        this.showView(this.oldViewname);
    }

    closeFetchLog(): void {
        this.showView(this.oldViewname);
    }

    openFetchLog(openFetchLog: boolean): void {
        this.utlsFileService.setOpenWhenFileIsFetched(true);
        this.showView(AppConstants.VIEW_FETCH_LOGS);
    }

    fetchLogsWithDate(fetchLogParam: FetchLogParam): void {
        if (fetchLogParam.isOk()) {
            this.fetchLogfile(fetchLogParam);
        }
        else {
            console.error('something went wrong with from and to-parameters...');
        }

    }

    showView(viewname: string) {
        let self = this;
        self.zone.run(() => {
            if (self.isServerIpUpdated(self)) {
                // update ip for server in menu
                self.ngOnInit();
            }

            if (self.activeViewname !== viewname && self.activeViewname !== AppConstants.VIEW_WAIT &&
                self.activeViewname !== AppConstants.VIEW_SETTINGS) {
                self.oldViewname = self.activeViewname;
            }
            for (let key in self.views) {
                let aView = self.views[key];
                aView.show = viewname === aView.name;
            }
            self.activeViewname = viewname;
            // console.log('after change, active:' + self.activeViewname + ' and oldactive is:' + self.oldViewname);
        });
    }

    private isServerIpUpdated(component): boolean {
        return component.activeViewname === AppConstants.VIEW_SETTINGS && component.oldViewname !== AppConstants.VIEW_SETTINGS &&
            component.menu.items[0].submenu.items[2].label !== component.getIpMenuLabel();
    }

    fetchLogfile(fetchLogParam: FetchLogParam) {
        let show = this.utlsFileService.isOpenWhenFileIsFetched();
        console.log('fetching logfile and will show immediately?' + show);
        let self = this;
        self.zone.run(() => {
                self.showView(AppConstants.VIEW_WAIT);
                let observableResult = self.utlsFileService.fetchLogs(fetchLogParam);
                let logSubscription = observableResult.subscribe(
                    result => {
                        if (result.isOk) {
                            let filepath = electron.remote.app.getAppPath();
                            this.alertLog('Logfile with name :' + result.value + ' is saved at location:\n' + filepath);
                            if (show) {
                                let fileAndPath = filepath + '/' + result.value;
                                self.createLogContentFromFile(fileAndPath, this.utlsFileService.createLogsFromFile.bind(this.utlsFileService));
                            }
                            else {
                                self.showView(this.oldViewname);
                            }
                        }
                        else {
                            this.alertLog(result.value);
                            self.showView(this.oldViewname);
                        }
                        logSubscription.unsubscribe();
                    },
                    error => {
                        this.alertLog('app-component, logsubscription error:' + error);
                        logSubscription.unsubscribe();
                        self.showView(this.oldViewname);
                    },
                    () => {
                        console.log('app-component, logsubscription done:');
                        logSubscription.unsubscribe();
                        self.showView(this.oldViewname);
                    }
                );

            }
        );
    }


    public checkFilenameAndHandleFile = (fileNamesArr: Array<any>) => {
        if (!fileNamesArr) {
            this.alertLog("No file selected");
            this.showView(this.oldViewname);
        }
        console.log("filename selected:" + fileNamesArr[0]);
        this.createLogContentFromFile(fileNamesArr[0], this.utlsFileService.createLogsFromFile.bind(this.utlsFileService));
    }

    public createLogContentFromFile = (fileName: string, fetchLogFunction: Function) => {
        this.init();
        this.logfileName = fileName;
        this.logs$ = fetchLogFunction(fileName);

        let self = this;
        self.zone.run(() => {
            let logSubscription = this.logs$.subscribe(logs => {
                    self.setTimeData(logs);
                    logSubscription.unsubscribe();
                },
                error => {
                    console.log('Error while creating logs from file:' + error);
                    logSubscription.unsubscribe();
                });
        });
        this.showView(AppConstants.VIEW_LOGS);
        this.isLoaded = true;
    }

    setTimeData(logs: UtlsLog[]) {
        let timestampFrom;
        let timestampTo;
        _.forEach(logs, function (log) {
            if (!timestampFrom || timestampFrom > log.timestamp) {
                timestampFrom = log.timestamp;
            }
            else if (!timestampTo || timestampTo < log.timestamp) {
                timestampTo = log.timestamp;
            }
        });
        let firstDate = new Date(timestampFrom);
        let lastDate = new Date(timestampTo);

        this.timeFilterService.setFirstDateFromFile(firstDate);
        this.timeFilterService.setLastDateFromFile(lastDate);

        this.timeFilterService.resetTimefilter();

        console.log('new from:' + this.timeFilterService.getSelectedTimefilterFrom().asString() + ' new to:' +
            this.timeFilterService.getLastSelectedTimefilterTo().asString());
    }


    resetFilter() {
        let self = this;
        self.zone.run(() => {
            self.init();
            self.timeFilterService.resetTimefilter();
            self.logs$ = self.utlsFileService.getAllLogs();
        });
    }

    init() {
        // this.timeFilterService.resetAllDateValues();
        let self = this;
        self.zone.run(() => {
            this.filterQuery = "";
            this.selectedColumn = this.selectedColumnDefaultChoice;
            this.lastSelectedColumn = "";
            this.selectedContent = this.selectedContentDefaultChoice;
            this.columnContent = new Array<Dto>();
            this.columnSortObject = new SortingObject();
            this.columnSortObject.sortorder = AppConstants.COLUMN_SORT_DESC;
            this.columnSortObject.sortname = AppConstants.COL_TIMESTAMP;
            this.columnSortValue = AppConstants.COLUMN_SORT_DESC;
            this.sortBy = AppConstants.COL_TIMESTAMP;
        });
    }

    changeColumn(newColumn) {
        this.changeColumnValueAndContentValues(newColumn);
        if (AppConstants.STR_ALL === newColumn) {
            this.logs$ = this.utlsFileService.getAllLogs();
        }
    }

    private changeColumnValueAndContentValues(newColumn) {
        if (!_.isEqual(this.lastSelectedColumn, newColumn)) {
            this.lastSelectedColumn = newColumn;
            this.selectedColumn = newColumn;
            if (this.allColumns !== newColumn && this.selectedColumnDefaultChoice !== newColumn) {
                this.columnContent = this.utlsFileService.getContentForSpecificColumn(newColumn);
                this.selectedContent = this.selectedContentDefaultChoice;
            }
            if (this.allColumns === newColumn) {
                this.columnContent = new Array<Dto>();
            }
        }

    }

    private alertLog(log: string): void {
        console.log(log);
        alert(log);
    }


    resetSort(sortBy) {
        if (this.isSameColumn(sortBy)) {
            if (this.isColumnSortAsc(sortBy)) {
                this.columnSortValue = AppConstants.COLUMN_SORT_DESC;
            }
            else {
                this.columnSortValue = AppConstants.COLUMN_SORT_ASC;
            }
        }
        else {
            this.sortBy = sortBy;
            this.columnSortValue = AppConstants.COLUMN_SORT_ASC;
        }
        this.columnSortObject = new SortingObject();
        this.columnSortObject.sortorder = this.columnSortValue;
        this.columnSortObject.sortname = this.sortBy;
    }

    private isSameColumn(sortBy): boolean {
        return this.sortBy === sortBy;
    }

    isColumnSortAsc(sortBy): boolean {
        return this.sortBy === sortBy && this.columnSortValue === AppConstants.COLUMN_SORT_ASC;
    }

    isColumnSortDesc(sortBy): boolean {
        return this.sortBy === sortBy && this.columnSortValue === AppConstants.COLUMN_SORT_DESC;
    }

    changeLogContent(newValueFromSpecificColumn) {
        if (this.allContent !== newValueFromSpecificColumn && this.selectedContentDefaultChoice !== newValueFromSpecificColumn) {
            this.logs$ = this.utlsFileService.getLogsForSpecificColumnValue(newValueFromSpecificColumn);
        }
        if (this.allContent === newValueFromSpecificColumn) {
            this.logs$ = this.utlsFileService.getAllLogs();
        }
    }


    ngOnDestroy() {
    }

}