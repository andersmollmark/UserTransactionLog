import {Component, OnInit} from "@angular/core";
import {UtlsFileService} from "./utls-file.service";
import {remote, ipcRenderer} from "electron";
import {UtlsLog} from "./log";
import {Observable} from "rxjs/Observable";
import {Dto} from "./dto";
import {AppSettings} from "./app.settings";
import * as _ from "lodash";
import {FilterService} from "./filter.service";
import {SelectedDate} from "./selectedDate";

let {dialog} = remote;


@Component({
    selector: 'my-app',
    templateUrl: './app/app.component.html'
})
export class AppComponent implements OnInit {

    showTo: boolean = false;
    showFrom: boolean = false;
    showFromTime: boolean = false;
    showToTime: boolean = false;
    ismeridian:boolean = true;
    fromTime: Date = new Date();
    toTime: Date = new Date();

    logs$: Observable<UtlsLog[]>;
    public filterQuery;
    timestampSortValue: string = "";
    clock;
    public isLoaded: boolean;
    public sortBy = "timestampAsDate";
    public sortOrder = "asc";

    timestampFrom;
    timestampTo;
    lastSelectedDateFrom: SelectedDate;
    lastSelectedDateTo: SelectedDate;
    selectedDateFrom: SelectedDate;
    selectedDateTo: SelectedDate;

    //Select-column-filter
    selectedColumnDefaultChoice = "--- Select column ---";
    public selectedColumn = this.selectedColumnDefaultChoice;
    allColumns = "All";
    cols = [
        {name: "Username", value: UtlsFileService.USERNAME},
        {name: "Active tab", value: UtlsFileService.TAB},
        {name: "Category", value: UtlsFileService.CATEGORY},
        {name: "Eventname", value: UtlsFileService.EVENTNAME}
    ];

    //Selected-column-filter
    selectedContentDefaultChoice = "--- Select ---";
    public selectedContent = this.selectedContentDefaultChoice;
    allContent = "All";
    columnContent: Dto[];


    constructor(private utlsFileService: UtlsFileService, private filterService: FilterService) {
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

    ngDoCheck(){
        this.filterQuery = this.filterService.getFilterQuery();
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
            this.logs$ = this.utlsFileService.createLogs(fileNamesArr[0]);
            let self = this;
            this.logs$.subscribe(logs => {
                _.forEach(logs, function (log) {
                    if(!self.timestampFrom || self.timestampFrom > log.timestamp){
                        self.timestampFrom = log.timestamp;
                    }
                    else if(!self.timestampTo || self.timestampTo < log.timestamp){
                        self.timestampTo = log.timestamp;
                    }
                });
                self.selectedDateTo = SelectedDate.getFromDate(new Date(self.timestampTo));
                self.selectedDateFrom = SelectedDate.getFromDate(new Date(self.timestampFrom));
                self.lastSelectedDateFrom = SelectedDate.getFromDateParts(self.selectedDateFrom.value);
                self.lastSelectedDateTo = SelectedDate.getFromDateParts(self.selectedDateTo.value);
                this.timestampSortValue = AppSettings.TIMESTAMP_SORT_DESC;
                this.sortOrder = "";
                console.log('new from:' + self.selectedDateFrom.asString() + ' new to:' + self.selectedDateTo.asString());
            });
            this.isLoaded = true;
        }
    };

    resetDateValues(){
        this.timestampFrom = undefined;
        this.timestampTo = undefined;
        this.selectedDateFrom = undefined;
        this.selectedDateTo = undefined;
        this.lastSelectedDateFrom = undefined;
        this.lastSelectedDateTo = undefined;
    }

    changeColumn(newColumn) {
        this.selectedColumn = newColumn;
        if (this.allColumns !== newColumn && this.selectedColumnDefaultChoice !== newColumn) {
            this.columnContent = this.utlsFileService.getContentForSpecificColumn(newColumn);
            this.selectedContent = this.selectedContentDefaultChoice;
        }
        if (this.allColumns === newColumn) {
            this.columnContent = new Array<Dto>();
            this.logs$ = this.utlsFileService.getAllLogs();
        }
    }

    changeTimestampSort() {
        if (this.isTimestampSortAsc()) {
            this.timestampSortValue = AppSettings.TIMESTAMP_SORT_DESC;
        }
        else{
            this.timestampSortValue = AppSettings.TIMESTAMP_SORT_ASC;
        }
        this.sortOrder = "";
    }

    getFromFilterDateString(): string{
        return this.selectedDateFrom ? this.selectedDateFrom.asString() : '';
    }

    getToFilterDateString(): string{
        return this.selectedDateTo ? this.selectedDateTo.asString() : '';
    }

    setShowFrom(){
        this.showFrom = true;
    }

    setShowFromTime(){
        this.showFromTime = true;
    }

    isShowFrom(){
        return this.showFrom;
    }

    isShowFromTime(){
        return this.showFromTime;
    }

    changeFrom(){
        if(this.selectedDateFrom){
            if(!this.selectedDateFrom.isSame(this.lastSelectedDateFrom)){
                this.lastSelectedDateFrom = SelectedDate.getFromDateParts(this.selectedDateFrom.value);
                this.showFrom = false;
                this.showFromTime = true;
            }
        }
    }

    changeFromTime(){
        if(this.fromTime && this.selectedDateFrom){
            if(!this.selectedDateFrom.isSameTime(this.fromTime)){
                this.selectedDateFrom.setTime(this.fromTime, 0);
            }
            this.filterService.setTimefilterFrom(this.selectedDateFrom.value);
            this.filterService.setFilterQuery(AppSettings.TIMESTAMP_FILTER_FROM.concat(this.selectedDateFrom.asString()));
            this.showFromTime = false;
        }
    }

    changeTo(){
        if(this.selectedDateTo){
            if(!this.selectedDateTo.isSame(this.lastSelectedDateTo)){
                this.lastSelectedDateTo = SelectedDate.getFromDateParts(this.selectedDateTo.value);
                this.showTo = false;
                this.showToTime = true;
            }
        }

    }

    changeToTime(){
        if(this.toTime && this.selectedDateTo){
            if(!this.selectedDateTo.isSameTime(this.toTime)){
                this.selectedDateTo.setTime(this.toTime, 59);
            }
            this.filterService.setTimefilterTo(this.selectedDateTo.value);
            this.filterService.setFilterQuery(AppSettings.TIMESTAMP_FILTER_TO.concat(this.selectedDateTo.asString()));
            this.showToTime = false;
        }
    }

    setShowTo(){
        this.showTo = true;
    }

    setShowToTime(){
        this.showToTime = true;
    }

    isShowTo(){
        return this.showTo;
    }


    isShowToTime(){
        return this.showToTime;
    }



    isTimestampSortAsc(){
        return this.timestampSortValue === AppSettings.TIMESTAMP_SORT_ASC;
    }

    isTimestampSortDesc(){
        return this.timestampSortValue === AppSettings.TIMESTAMP_SORT_DESC;
    }

    resetSort(){
        this.sortOrder = "asc";
        this.timestampSortValue = "";
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