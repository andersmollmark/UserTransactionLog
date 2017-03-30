import {Component, OnInit, NgZone} from "@angular/core";
import {UtlsFileService} from "./utls-file.service";
import {UtlsLog} from "./log";
import {Observable} from "rxjs/Observable";
import {Dto} from "./dto";
import {AppConstants} from "./app.constants";
import * as _ from "lodash";
import {TimeFilterService} from "./timefilter.service";
import {SortingObject} from "./sortingObject";
import {UtlserverService} from "./utlserver.service";

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
    columnContent: Dto[];

    showSettings: boolean = false;
    showLogs: boolean = false;


    constructor(private utlsFileService: UtlsFileService, private timeFilterService: TimeFilterService,
                private utlserverService: UtlserverService, private zone: NgZone) {
        this.isLoaded = false;
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
                    label: 'Change ip for utlserver (now:'.concat(self.utlserverService.getUtlsIp()).concat(')'),
                    click: function () {
                        self.zone.run(() => self.setShowSettings(true));
                    }
                },
                {
                    label: 'Fetch logfile',
                    click: function () {
                        self.zone.run(() => {
                                self.utlsFileService.fetchLogs();
                                self.setShowSettings(false);
                            }
                        );
                    }
                }
            ]
        }]);
        remote.Menu.setApplicationMenu(menu);
    }

    ngDoCheck() {
        this.filterQuery = this.timeFilterService.getFilterQuery();
        if (this.utlsFileService.isColumnContentChanged()) {
            this.changeColumnValueAndContentValues(this.selectedColumn);
        }
    }

    closeSettings(): void {
        this.setShowSettings(false);
    }

    setShowSettings(show: boolean): void {
        this.showSettings = show;
        this.showLogs = !show;
        console.log("showLogs:" + this.showLogs + " showSettings:" + this.showSettings);
    }

    setShowLogs(show: boolean): void {
        this.setShowSettings(!show);
    }


    public handleFile = (fileNamesArr: Array<any>) => {
        if (!fileNamesArr) {
            console.log("No file selected");
        }
        else {
            console.log("filename selected:" + fileNamesArr[0]);
            this.init();
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
            this.setShowLogs(true);
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
        if(!_.isEqual(this.lastSelectedColumn, newColumn)){
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