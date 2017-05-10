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
                            dialog.showOpenDialog(self.handleFile);
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
                        self.zone.run(() => {
                                self.showView(AppConstants.VIEW_WAIT);
                                let observableResult = self.utlsFileService.fetchLogs();
                                observableResult.subscribe(
                                    result => {
                                        if(result.isOk){
                                            console.log('Logfile with name :' + result.value + ' is saved');
                                            alert('Logfile with name :' + result.value + ' is saved');
                                        }
                                        else{
                                            console.log(result.value);
                                            alert(result.value);
                                        }
                                        self.zone.run(() => self.showView(self.oldViewname));
                                    }
                                );
                            }
                        );
                    }
                }
            ]
        }]);
        remote.Menu.setApplicationMenu(menu);

    }

    ngDoCheck() {
        console.log('ngdocheck...');
        this.filterQuery = this.timeFilterService.getFilterQuery();
        if (this.utlsFileService.isColumnContentChanged()) {
            this.changeColumnValueAndContentValues(this.selectedColumn);
        }
    }

    closeSettings(): void {
        this.showView(this.oldViewname);
    }

    showView(viewname: string) {
        console.log('show ' + viewname + ' and active now is:' + this.activeViewname + ' and oldactive is:' + this.oldViewname);
        if (this.activeViewname !== viewname) {
            this.oldViewname = this.activeViewname;
        }
        for (let key in this.views) {
            let aView = this.views[key];
            aView.show = viewname === aView.name;
        }
        this.activeViewname = viewname;
    }

    public handleFile = (fileNamesArr: Array<any>) => {
        if (!fileNamesArr) {
            console.log("No file selected");
        }
        else {
            console.log("filename selected:" + fileNamesArr[0]);
            this.init();
            this.logfileName = fileNamesArr[0];
            this.logs$ = this.utlsFileService.createLogs(fileNamesArr[0]);
            let timestampFrom;
            let timestampTo;

            let self = this;
            self.zone.run(() => {
                this.logs$.subscribe(logs => {
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
                });
            });
            this.showView(AppConstants.VIEW_LOGS);
            this.isLoaded = true;
        }
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