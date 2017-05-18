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
        let menu = remote.Menu.buildFromTemplate([{
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
                    label: 'Change ip for utlserver (now: '.concat(self.utlserverService.getUtlsIp()).concat(')'),
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
                }
            ]
        }]);
        remote.Menu.setApplicationMenu(menu);

    }

    ngDoCheck() {
        // console.log('ngdocheck...');
        this.filterQuery = this.timeFilterService.getFilterQuery();
        if (this.utlsFileService.isColumnContentChanged()) {
            this.changeColumnValueAndContentValues(this.selectedColumn);
        }
    }

    closeSettings(): void {
        this.showView(this.oldViewname);
    }

    closeFetchLog(): void {
        this.showView(this.oldViewname);
    }

    openFetchLog(openFetchLog: boolean): void{
        this.utlsFileService.setOpenWhenFileIsFetched(true);
        this.showView(AppConstants.VIEW_FETCH_LOGS);
    }

    fetchLogsWithDate(fetchLogParam: FetchLogParam): void {
        if(fetchLogParam.isOk()){
            this.fetchLogfile(fetchLogParam.getFrom(), fetchLogParam.getTo());
        }
        else{
            console.error('something went wrong with from and to-parameters...');
        }

    }

    showView(viewname: string) {
        let self = this;
        self.zone.run(() => {
            if (self.activeViewname !== viewname) {
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

    fetchLogfile(from: Date, to: Date) {
        let show = this.utlsFileService.isOpenWhenFileIsFetched();
        console.log('fetching logfile and will show immediately?' + show);
        let self = this;
        self.zone.run(() => {
                self.showView(AppConstants.VIEW_WAIT);
                let observableResult = self.utlsFileService.fetchLogs(from, to);
                let logSubscription = observableResult.subscribe(
                    result => {
                        if (result.isOk) {
                            console.log('Logfile with name :' + result.value + ' is saved');
                            alert('Logfile with name :' + result.value + ' is saved');
                        }
                        else {
                            console.log(result.value);
                            alert(result.value);
                        }
                        if (show) {
                            self.createLogContentFromFile(result.value);
                        }
                        else {
                            self.showView(this.oldViewname);
                        }
                     logSubscription.unsubscribe();
                    },
                    error => {
                        console.error('app-component, logsubscription error:' + error);
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
            console.log("No file selected");
            this.showView(this.oldViewname);
        }
        else {
            console.log("filename selected:" + fileNamesArr[0]);
            this.createLogContentFromFile(fileNamesArr[0]);
        }
    }

    public createLogContentFromFile = (fileName: string) => {
        console.log("creating logcontent from:" + fileName);
        this.init();
        this.logfileName = fileName;
        this.logs$ = this.utlsFileService.createLogs(fileName);
        let timestampFrom;
        let timestampTo;

        let self = this;
        self.zone.run(() => {
            let logSubscription = this.logs$.subscribe(logs => {
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

                self.timeFilterService.setFirstDateFromFile(firstDate);
                self.timeFilterService.setLastDateFromFile(lastDate);

                self.timeFilterService.resetTimefilter();

                console.log('new from:' + self.timeFilterService.getSelectedTimefilterFrom().asString() + ' new to:' +
                    self.timeFilterService.getLastSelectedTimefilterTo().asString());

                logSubscription.unsubscribe();

            });
        });
        this.showView(AppConstants.VIEW_LOGS);
        this.isLoaded = true;
    }


    resetFilter() {
        this.init();
        this.timeFilterService.resetTimefilter();
        this.logs$ = this.utlsFileService.getAllLogs();
    }

    init() {
        // this.timeFilterService.resetAllDateValues();
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