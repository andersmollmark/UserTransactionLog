import {Component, OnInit} from "@angular/core";
import {UtlsFileService} from "./utls-file.service";
import {remote, ipcRenderer} from "electron";
import {UtlsLog} from "./log";
import {Observable} from "rxjs/Observable";
import {Dto} from "./dto";
import {AppConstants} from "./app.constants";
import * as _ from "lodash";
import {TimeFilterService} from "./timefilter.service";
import {SortingObject} from "./sortingObject";

let {dialog} = remote;


@Component({
    selector: 'my-app',
    templateUrl: './app/app.component.html'
})
export class AppComponent implements OnInit {

    logs$: Observable<UtlsLog[]>;
    public filterQuery;
    columnSortValue: string = "";
    columnSortObject: SortingObject = new SortingObject();
    clock;
    public isLoaded: boolean;
    public sortBy = "";

    timestampFrom;
    timestampTo;

    //Select-column-filter
    selectedColumnDefaultChoice = "--- Select column ---";
    public selectedColumn = this.selectedColumnDefaultChoice;
    allColumns = AppConstants.STR_ALL;
    cols = [
        {name: "Username", value: AppConstants.COL_USERNAME},
        {name: "Active tab", value: AppConstants.COL_TAB},
        {name: "Category", value: AppConstants.COL_CATEGORY},
        {name: "Eventname", value: AppConstants.COL_EVENTNAME}
    ];

    //Selected-column-filter
    selectedContentDefaultChoice = "--- Select ---";
    public selectedContent = this.selectedContentDefaultChoice;
    allContent = AppConstants.STR_ALL;
    columnContent: Dto[];


    constructor(private utlsFileService: UtlsFileService, private timeFilterService: TimeFilterService) {
        this.isLoaded = false;
    }

    ngOnInit(): void {
        let self = this;
        let menu = remote.Menu.buildFromTemplate([{
            label: 'Menu',
            submenu: [
                {
                    label: 'open logfile',
                    click: function () {
                        dialog.showOpenDialog(self.handleFile);
                    }
                }
            ]
        }]);
        remote.Menu.setApplicationMenu(menu);

        this.clock = Observable.interval(1000);
    }

    ngDoCheck() {
        this.filterQuery = this.timeFilterService.getFilterQuery();
        if(this.utlsFileService.isColumnContentChanged()){
            this.changeColumnValueAndContentValues(this.selectedColumn);
        }
    }


    public handleFile = (fileNamesArr: Array<any>) => {
        if (!fileNamesArr) {
            console.log("No file selected");
        }
        else {
            console.log("filename selected:" + fileNamesArr[0]);
            this.resetDateValues();
            this.filterQuery = "";
            this.selectedColumn = this.selectedColumnDefaultChoice;
            this.selectedContent = this.selectedContentDefaultChoice;
            this.columnContent = new Array<Dto>();
            this.logs$ = this.utlsFileService.createLogs(fileNamesArr[0]);
            let self = this;
            this.logs$.subscribe(logs => {
                _.forEach(logs, function (log) {
                    if (!self.timestampFrom || self.timestampFrom > log.timestamp) {
                        self.timestampFrom = log.timestamp;
                    }
                    else if (!self.timestampTo || self.timestampTo < log.timestamp) {
                        self.timestampTo = log.timestamp;
                    }
                });
                self.timeFilterService.setSelectedTimefilterFrom(new Date(self.timestampFrom));
                self.timeFilterService.setLastSelectedTimefilterFrom(new Date(self.timestampFrom));

                self.timeFilterService.setSelectedTimefilterTo(new Date(self.timestampTo));
                self.timeFilterService.setLastSelectedTimefilterTo(new Date(self.timestampTo));

                this.columnSortObject = new SortingObject();
                this.columnSortObject.sortorder = AppConstants.COLUMN_SORT_DESC;
                this.columnSortObject.sortname = AppConstants.COL_TIMESTAMP;
                this.columnSortValue = AppConstants.COLUMN_SORT_DESC;
                this.sortBy = AppConstants.COL_TIMESTAMP;

                console.log('new from:' + self.timeFilterService.getSelectedTimefilterFrom().asString() + ' new to:' +
                    self.timeFilterService.getLastSelectedTimefilterTo().asString());
            });
            this.isLoaded = true;
        }
    };

    resetDateValues() {
        this.timestampFrom = undefined;
        this.timestampTo = undefined;
        this.timeFilterService.resetAllDateValues();
    }

    changeColumn(newColumn) {
        this.changeColumnValueAndContentValues(newColumn);
        if (AppConstants.STR_ALL === newColumn) {
            this.logs$ = this.utlsFileService.getAllLogs();
        }
    }

    private changeColumnValueAndContentValues(newColumn) {
        this.selectedColumn = newColumn;
        if (this.allColumns !== newColumn && this.selectedColumnDefaultChoice !== newColumn) {
            this.columnContent = this.utlsFileService.getContentForSpecificColumn(newColumn);
            this.selectedContent = this.selectedContentDefaultChoice;
        }
        if (this.allColumns === newColumn) {
            this.columnContent = new Array<Dto>();
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