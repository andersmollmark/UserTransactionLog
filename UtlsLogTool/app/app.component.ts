import {Component, NgZone, OnDestroy, OnInit} from "@angular/core";
import {UtlsFileService} from "./utls-file.service";
import {UtlsLog} from "./log";
import {Observable} from "rxjs/Observable";
import {Dto} from "./dto";
import {AppConstants} from "./app.constants";
import {TimeFilterService} from "./timefilter.service";
import {SortingObject} from "./sortingObject";
import {UtlserverService} from "./utlserver.service";
import {View} from "./view";
import {FetchLogParam} from "./fetchLogParam";
import {TimeHandler} from "./time-handler";
import {Subscription} from "rxjs/Subscription";
import {ColumnFilter} from "./column-filter";
import {LogActionHandler} from "./log-action-handler";
import {CreateAllLogsAction} from "./create-all-logs-action";
import {ResetAllLogsAction} from "./reset-all-logs-action";
import {GetAllLogsAction} from "./get-all-logs-action";

const electron = require('electron');
const remote = electron.remote;
const ipcRenderer = electron.ipcRenderer;

let {dialog} = remote;

@Component({
    selector: 'my-app',
    templateUrl: './app.component.html'

})
export class AppComponent implements OnInit, OnDestroy {

    logs$: Observable<UtlsLog[]>;
    activeLogs: UtlsLog[] = null;
    logsTimezoneId: string;
    currentTimezoneId: string;
    currentTimezoneIdText: string;
    timezoneDisabled: boolean = false;
    oldTimezoneId: string;
    timezones = [];
    public filterQuery;

    activePage: number = 1;

    columnSortValue: string = "";
    columnSortObject: SortingObject = new SortingObject();

    public isLoaded: boolean;
    public sortBy = "";
    logfileName: string = '';

    // Select-column-filter

    lastSelectedColumn: Dto = null;
    cols = [new Dto(AppConstants.STR_ALL, AppConstants.STR_ALL),
        new Dto("Username", AppConstants.COL_USERNAME),
        new Dto("Active tab", AppConstants.COL_TAB),
        new Dto("Category", AppConstants.COL_CATEGORY),
        new Dto("Eventname", AppConstants.COL_EVENTNAME)
    ];

    public selectedColumn = this.cols[0];
    columnFilter: ColumnFilter = new ColumnFilter(this.cols[0]);
    // Selected-column-filter
    public selectedContent = AppConstants.STR_ALL;
    constants = AppConstants;
    columnContent: Dto[];

    oldViewname: string;
    activeViewname: string = AppConstants.VIEW_EMPTY;
    views: View[] = new Array<View>();

    private hourMode12Subscription: Subscription = null;

    private logSubscription: Subscription = null;

    menu;

    constructor(private utlsFileService: UtlsFileService, public timeFilterService: TimeFilterService,
                private utlserverService: UtlserverService, private zone: NgZone) {
        this.isLoaded = false;
        this.initViews();
        this.hourMode12Subscription = this.timeFilterService.get12HourModeSubscription().subscribe(mode12Hour => {
            this.zone.run(() => {
                this.changeHourMode();
            });
        });
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
                    label: 'Fetch logs',
                    click: function () {
                        self.utlsFileService.setOpenWhenFileIsFetched(true);
                        self.showView(AppConstants.VIEW_FETCH_LOGS);
                    }
                },
                {
                    label: 'Shift to 12hr mode',
                    click: function () {
                        self.timeFilterService.set12HrMode(self.currentTimezoneId);

                    }
                },
                {
                    label: 'Shift to 24hr mode',
                    click: function () {
                        self.timeFilterService.set24HrMode(self.currentTimezoneId);
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

    private changeHourMode(): void {
        let timezoneHandler = TimeHandler.getInstance();
        if (this.activeLogs !== null) {
            this.zone.run(() => {
                timezoneHandler.changeHourMode(this.currentTimezoneId, this.activeLogs);
            });
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
                let fetchLogSubscription = observableResult.subscribe(
                    result => {
                        if (result.isOk) {
                            let filepath = electron.remote.app.getAppPath();
                            this.alertLog('Logfile with name :' + result.value + ' is saved at location:\n' + filepath);
                            if (show) {
                                let fileAndPath = filepath + '/' + result.value;
                                self.createLogContentFromFile(fileAndPath);
                            }
                            else {
                                self.showView(this.oldViewname);
                            }
                        }
                        else {
                            this.alertLog(result.value);
                            self.showView(this.oldViewname);
                        }
                        fetchLogSubscription.unsubscribe();
                    },
                    error => {
                        this.alertLog('app-component, logsubscription error:' + error);
                        fetchLogSubscription.unsubscribe();
                        self.showView(this.oldViewname);
                    },
                    () => {
                        console.log('app-component, logsubscription done:');
                        fetchLogSubscription.unsubscribe();
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
            return;
        }
        console.log("filename selected:" + fileNamesArr[0]);
        this.createLogContentFromFile(fileNamesArr[0]);
    }

    public createLogContentFromFile = (fileName: string) => {
        this.init();
        this.logfileName = fileName;
        this.utlsFileService.createLogsFromFile(fileName);
        if(this.logSubscription !== null) {
            this.logSubscription.unsubscribe();
        }


        let actionHandler = LogActionHandler.getInstance();
        actionHandler.setNext(new CreateAllLogsAction(this));

        this.zone.run(() => {
            this.logs$ = this.utlsFileService.subscribeOnLogChanges();
            this.logSubscription = this.logs$.subscribe(logs => {

                    if(actionHandler.hasNext()) {
                        actionHandler.next().execute(logs);
                        if (this.timeFilterService.getSelectedTimefilterFrom() !== null && this.timeFilterService.getSelectedTimefilterTo() !== null) {
                            console.log('new from:' + this.timeFilterService.getSelectedTimefilterFrom().asString() + ' new to:' +
                                this.timeFilterService.getSelectedTimefilterTo().asString());

                        }

                    }


                },
                error => {
                    console.log('Error while subscribing on log-changes, error:' + error);
                    this.logSubscription.unsubscribe();
                });

        });
        this.showView(AppConstants.VIEW_LOGS);
        this.isLoaded = true;
    }

    setTimezones() {
        let momentTz = require("moment-timezone");
        this.timezones = momentTz.tz.names();
        if (this.utlsFileService.getActiveTimezoneId() !== null) {
            this.currentTimezoneId = this.utlsFileService.getActiveTimezoneId();
            this.currentTimezoneIdText = this.currentTimezoneId;
            this.timezoneDisabled = false;
        }
        else {
            this.currentTimezoneId = 'Europe/Stockholm';
            this.currentTimezoneIdText = 'Unknown, using ' + this.currentTimezoneId;
            this.timezoneDisabled = true;
        }
        this.oldTimezoneId = this.currentTimezoneId;
        this.logsTimezoneId = this.currentTimezoneId;

    }

    resetTimezone(): void {
        this.changeTimezone(this.logsTimezoneId);
    }

    changeTimezone(newTimezone: string): void {
        this.currentTimezoneId = newTimezone;
        this.zone.run(() => {
            this.timeFilterService.changeTimezone(newTimezone, this.activeLogs);
            this.oldTimezoneId = this.currentTimezoneId;
        });
    }

    setTimeData(logs: UtlsLog[]) {

        let initTimezoneResult = TimeHandler.getInstance().initTimezonesAndGetEndDates(this.currentTimezoneId, logs);

        let firstDate = new Date(initTimezoneResult.firstTimestamp);
        let lastDate = new Date(initTimezoneResult.lastTimestamp);

        this.timeFilterService.setCurrentTimezone(this.currentTimezoneId);
        this.timeFilterService.setLogsTimezone(this.logsTimezoneId);
        this.timeFilterService.setFirstDateFromFile(firstDate);
        this.timeFilterService.setLastDateFromFile(lastDate);

    }

    resetFilter() {
        this.zone.run(() => {
            this.currentTimezoneId = this.logsTimezoneId;
            this.init();
            LogActionHandler.getInstance().setNext(new ResetAllLogsAction(this));
            this.utlsFileService.getAllLogs();
        });
    }

    init() {
        this.zone.run(() => {
            this.filterQuery = "";
            this.activePage = 1;
            this.columnFilter = new ColumnFilter(this.cols[0]);
            this.selectedColumn = this.cols[0];
            this.lastSelectedColumn = null;
            this.selectedContent = AppConstants.STR_ALL;
            this.columnContent = new Array<Dto>();
            this.columnSortObject = new SortingObject();
            this.columnSortObject.sortorder = AppConstants.COLUMN_SORT_DESC;
            this.columnSortObject.sortname = AppConstants.COL_TIMESTAMP;
            this.columnSortValue = AppConstants.COLUMN_SORT_DESC;
            this.sortBy = AppConstants.COL_TIMESTAMP;
        });
    }

    changeColumn(newColumn: Dto): void {
        this.changeColumnValueAndContentValues(newColumn);
        if (AppConstants.STR_ALL === newColumn.value) {
            LogActionHandler.getInstance().setNext(new GetAllLogsAction(this));
            this.utlsFileService.getAllLogs();
        }
    }

    private changeColumnValueAndContentValues(newColumn: Dto): void {
        if (this.lastSelectedColumn === null || !this.lastSelectedColumn.equals(newColumn)) {
            this.lastSelectedColumn = newColumn;
            this.selectedColumn = newColumn;
            if (AppConstants.STR_ALL === newColumn.value) {
                this.columnContent = new Array<Dto>();
            }
            else {
                this.columnContent = this.utlsFileService.getContentForSpecificColumn(newColumn);
                this.selectedContent = AppConstants.STR_ALL;
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

    changeLogContent(newValueFromSpecificColumn: Dto) {
        if (AppConstants.STR_ALL !== newValueFromSpecificColumn.value) {
            this.columnFilter = new ColumnFilter(newValueFromSpecificColumn);
        }
        LogActionHandler.getInstance().setNext(new GetAllLogsAction(this));
        this.utlsFileService.getAllLogs();
    }


    ngOnDestroy() {
        if (this.hourMode12Subscription !== null) {
            this.hourMode12Subscription.unsubscribe();
        }
        if (this.logSubscription !== null) {
            this.logSubscription.unsubscribe();
        }
    }

}